 /*
  * Copyright (c) 2009, Julian Gosnell
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *     * Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *
  *     * Redistributions in binary form must reproduce the above
  *     copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided
  *     with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.dada.demo;
 
 import java.awt.GridLayout;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.rmi.server.UID;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Session;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.apache.activemq.ActiveMQConnectionFactory;
 import org.dada.core.MetaModel;
 import org.dada.core.Model;
 import org.dada.core.Registration;
 import org.dada.core.Update;
 import org.dada.core.View;
 import org.dada.jms.RemotingFactory;
 import org.dada.slf4j.Logger;
 import org.dada.slf4j.LoggerFactory;
 
 public class Client {
 
 	private static final int ONE_MINUTE = 60000;
 	private static final Logger LOG = LoggerFactory.getLogger(Client.class);
 	private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(20); // TODO: why is this static ?
 
 	private final String serverName;
 	private final String modelName;
 	private final Session session;
 	private final int timeout;
 	private final boolean topLevel;
 	private final TableModelView<Object, Object> guiModel;
 	private final Destination serverDestination;
 	private final MetaModel metaModel;
 	private final Model<Object, Object> serverProxy;
 	private final Destination clientDestination;
 	private final RemotingFactory<View<Object>> serverFactory;
 	private final View<Object> clientServer;
 	private final JView jview;
 	private final JTable table;
 	private final JFrame frame;
 	private final JPanel panel;
 
 	private int selected  = -1;
 
 	public Client(String serverName, MetaModel metaModel, String modelName, Session session, int timeout, boolean topLevel) throws JMSException {
 		this.serverName = serverName;
 		this.modelName = modelName;
 		this.session = session;
 		this.timeout = timeout;
 		this.topLevel = topLevel;
 
		guiModel = new TableModelView<Object, Object>();
 		LOG.info("viewing: " + this.modelName);
 
 		serverDestination = session.createQueue(this.modelName);
 		
 		if (metaModel == null) {
 			RemotingFactory<MetaModel> remotingFactory = new RemotingFactory<MetaModel>(session, MetaModel.class, timeout);
 			this.metaModel = remotingFactory.createSynchronousClient(serverDestination, true);
 		} else {
 			this.metaModel = metaModel;
 		}
 		RemotingFactory<Model<Object, Object>> clientFactory = new RemotingFactory<Model<Object, Object>>(session, Model.class, timeout);
 		serverProxy = clientFactory.createSynchronousClient(serverDestination, true);
 
 		// create a Client
 
 		clientDestination = session.createQueue("Client." + new UID().toString()); // tie up this UID with the one in RemotingFactory
 		serverFactory = new RemotingFactory<View<Object>>(session, View.class, timeout);
 		serverFactory.createServer(guiModel, clientDestination, EXECUTOR_SERVICE);
 		clientServer = serverFactory.createSynchronousClient(clientDestination, true);
 
 		// pass the client over to the server to attach as a listener..
 		Registration<Object, Object> registration = this.metaModel.registerView(modelName, clientServer);
 		Collection<Object> models = registration.getData();
 		if (models != null) {
 			guiModel.setMetadata(registration.getMetadata());
 			Collection<Update<Object>> insertions = new ArrayList<Update<Object>>();
 			for (Object model : models)
 				insertions.add(new Update<Object>(null, model));
 			guiModel.update(insertions, new ArrayList<Update<Object>>(), new ArrayList<Update<Object>>());
 		} else
 			LOG.warn("null MODEL content returned");
 		LOG.info("Client ready: " + clientDestination);
 
 		jview = new JView(guiModel);
 		table = jview.getTable();
 
 		ListSelectionModel selectionModel = table.getSelectionModel();
 		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		selectionModel.addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				if (!e.getValueIsAdjusting()) {
 					int row = table.getSelectedRow();
 					LOG.trace("SELECTION CHANGED: " + row);
 					selected = row;
 				}
 			}
 		});
 		table.addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 
 			}
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				if (e.getClickCount() == 2) {
 					String targetModelName = (String) guiModel.getValueAt(selected, 0);
 					LOG.info("Opening: " + targetModelName);
 					try {
 						new Client(Client.this.serverName, Client.this.metaModel, targetModelName, Client.this.session, Client.this.timeout, false);
 					} catch (JMSException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 				}
 			}
 
 			@Override
 			public void mouseExited(MouseEvent e) {
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 
 		frame = new JFrame(this.modelName);
 		panel = new JPanel();
 		//new BoxLayout(panel, BoxLayout.Y_AXIS);
 		panel.setLayout(new GridLayout());
 		panel.add(jview);
 		panel.setOpaque(true);
 		frame.setContentPane(panel);
 		frame.pack();
 		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent event) {
 				LOG.info("Closing: " + Client.this.modelName);
 				Client.this.metaModel.deregisterView(Client.this.modelName, clientServer);
 				if (Client.this.topLevel)
 					System.exit(0);
 			}
 		});
 		frame.setVisible(true);
 	}
 
 	/**
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		// TODO Auto-generated method stub
 		final String serverName = (args.length == 0 ? "Server" : args[0]);
 		//String url = "peer://" + serverName + "/broker0?broker.persistent=false&useJmx=false";
 		String url = "tcp://localhost:61616";
 		LOG.info("Broker URL: " + url);
 		final ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
 		final Connection connection = connectionFactory.createConnection();
 		connection.start();
 		final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 		SwingUtilities.invokeAndWait(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					new Client(serverName, null, serverName + ".MetaModel", session, ONE_MINUTE, true);
 				} catch (JMSException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		// tidy up...
 	}
 
 }
