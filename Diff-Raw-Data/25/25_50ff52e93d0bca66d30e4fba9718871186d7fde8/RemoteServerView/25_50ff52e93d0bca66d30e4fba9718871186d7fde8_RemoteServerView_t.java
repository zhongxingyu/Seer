 /*
  * Copyright (C) 2012 Pauli Kauppinen
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package example.swingclient;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import org.javnce.upnp.RemoteServerInfo;
 import org.javnce.upnp.UpnpClient;
 import org.javnce.upnp.UpnpClientObserver;
 
 /**
  * The view for showing found remote servers.
  */
 public class RemoteServerView extends JPanel implements UpnpClientObserver, ActionListener {
 
     /**
     *
     */
    private static final long serialVersionUID = -411909184007381186L;
    /**
      * The table model.
      */
     final private RemoteServerInfoTableModel tableModel;
     /**
      * The table view.
      */
     final private JTable table;
     /**
      * The controller.
      */
     final private UpnpClient upnp;
     /**
      * The parent.
      */
     final private SwingClient parent;
     /**
      * The button.
      */
     final private JButton button;
 
     /**
      * Instantiates a new remote server view.
      *
      * @param parent the parent
      */
     public RemoteServerView(SwingClient parent) {
         super();
         setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 
         upnp = new UpnpClient();
 
         this.parent = parent;
 
         tableModel = new RemoteServerInfoTableModel();
         table = new JTable();
         table.setModel(tableModel);
         table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         add(new JScrollPane(table));
 
         add(new JLabel(" "));
         add(new JLabel(" "));
         add(new JLabel("Select server"));
         add(new JLabel(" "));
         add(new JLabel(" "));
 
         button = new JButton("Connect");
         add(button);
         init();
     }
 
     /**
      * Inits the controller.
      */
     private void init() {
         button.addActionListener(this);
         upnp.setObserver(this);
         new Thread(upnp).start();
     }
 
     /* (non-Javadoc)
      * @see org.javnce.upnp.client.UpnpClientObserver#serverFound(org.javnce.upnp.client.RemoteServerInfo)
      */
     @Override
     public void serverFound(RemoteServerInfo server) {
         final RemoteServerInfo item = server;
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 tableModel.addRemoteServerInfo(item);
             }
         });
     }
 
     /* (non-Javadoc)
      * @see org.javnce.upnp.client.UpnpClientObserver#serverLost(org.javnce.upnp.client.RemoteServerInfo)
      */
     @Override
     public void serverLost(RemoteServerInfo server) {
         //TODO ....
     }
 
     /* (non-Javadoc)
      * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     @Override
     public void actionPerformed(ActionEvent arg0) {
         int i = table.getSelectedRow();
         if (-1 != i) {
             parent.createVncView(this, tableModel.getRemoteServerInfo(i));
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     upnp.shutdown();
                 }
             }).start();
 
 
         }
     }
 }
