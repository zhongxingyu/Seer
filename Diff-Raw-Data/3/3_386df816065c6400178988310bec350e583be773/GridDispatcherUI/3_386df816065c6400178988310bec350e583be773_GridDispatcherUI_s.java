 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2010 SorcerSoft.org.
  *  
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package sorcer.core.grid.provider.grider;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Label;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.Serializable;
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.util.Vector;
 
 import javax.security.auth.Subject;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import net.jini.core.event.RemoteEvent;
 import net.jini.core.event.RemoteEventListener;
 import net.jini.core.event.UnknownEventException;
 import net.jini.core.lookup.ServiceItem;
 import net.jini.jeri.BasicILFactory;
 import net.jini.jeri.BasicJeriExporter;
 import net.jini.jeri.tcp.TcpServerEndpoint;
 import sorcer.core.FileStorer;
 import sorcer.core.context.ServiceContext;
 import sorcer.core.exertion.NetTask;
import sorcer.core.grid.provider.dispatcher.GridDispatcherAttribUI;
 import sorcer.core.signature.NetSignature;
 import sorcer.security.util.SorcerPrincipal;
 import sorcer.service.Context;
 import sorcer.service.ContextException;
 import sorcer.service.ServiceExertion;
 import sorcer.service.Servicer;
 import sorcer.util.DocumentDescriptor;
 import sorcer.util.SorcerUtil;
 import sorcer.util.rmi.OutputStreamProxy;
 
 public class GridDispatcherUI extends JFrame implements ActionListener {
 	// public class GridDispatcherUI extends SecureSorcerUI{
 
 	private JTextField browseTfld, fsInFolderTfld, fsInFileTfld,
 			fsOutFolderTfld, fsOutFileTfld, jSizeTfld, notifyTfld, jProgramfld;
 
 	private JTextArea inListTarea, outListTarea, jbSizeTarea;
 
 	private JComboBox jcBox;
 
 	private JButton runBtn, clearBtn, spcifyExecBtn, argBtn, attribBtn;
 
 	private GridDispatcherRemote dispatcher;
 
 	private Servicer dispatcher1;
 
 	private ServiceItem item;
 
 	private RemoteEventListener listener;
 
 	private OutFrame outFrame;
 
 	// private Subject loggedSubject;
 
 	// ----------------------------------BUILD GUI
 	// BEGIN------------------------------------
 	public GridDispatcherUI(Object obj) {// Object obj) {
 		// public GridDispatcherUI(){//Object obj) {
 		// super(obj);
 
 		// loggedSubject=null;
 		try {
 			// obj = getPreparedProxy();
 			if (obj == null) {
 				System.out.println(" *****OBJECT IS NULL***** ");
 			}
 
 			System.out.println("obj=" + obj);
 			this.item = (ServiceItem) obj;
 			dispatcher = (GridDispatcherRemote) item.service;
 
 			System.out.println("dispatcher=" + dispatcher);
 			// dispatcher = (GridDispatcherRemote)obj;
 			// dispatcher1 = (Servicer)obj;
 			dispatcher1 = (Servicer) dispatcher;
 
 			setTitle("SGrid Dispatcher");
 
 			// Building the right panel
 			JPanel ioPnl = new JPanel(new GridLayout(2, 1));
 			ioPnl.add(getFileStoreInputUI());
 			ioPnl.add(getFileStoreOutputUI());
 
 			JPanel listPnl = new JPanel(new GridLayout(1, 2));
 			listPnl.add(getInputListUI());
 			// listPnl.add(getOutputListUI()); // make it a popup window
 
 			JPanel rightPnl = new JPanel(new BorderLayout());
 			rightPnl.add(getInputFilePnl(), BorderLayout.NORTH);
 			rightPnl.add(new JScrollPane(listPnl), BorderLayout.CENTER);
 
 			getContentPane().setLayout(new GridLayout(1, 2));
 			getContentPane().add(new JScrollPane(getParamInputUI()));
 			getContentPane().add(rightPnl);
 
 			listener = new DispatcherListener(this).getListener();
 			// Display the window.
 			pack();
 			// this.setResizable(false);
 			setVisible(true);
 			outFrame = new OutFrame();
 		} catch (Exception e) {
 			System.out.println("Exception in Constructor");
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * public static void main(String args[]){ GridDispatcherUI dispatcherui =
 	 * new GridDispatcherUI(); //dispatcherui.setSize(2000,500);
 	 * dispatcherui.pack(); dispatcherui.show(); }
 	 */
 
 	private Vector parseInputFile() {
 		Vector v = new Vector();
 		BufferedReader in = null;
 
 		try {
 			String fileName = browseTfld.getText();
 			in = new BufferedReader(new FileReader(fileName));
 			String args;
 			while ((args = in.readLine()) != null)
 				v.add(args);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		} finally {
 			try {
 				if (in != null)
 					in.close();
 			} catch (Exception e) {
 			}
 		}
 		return v;
 	}
 
 	private JPanel getParamInputUI() {
 		JPanel pnl1, pnl2, pnl3, pnl4;
 
 		JPanel paramPnl = new JPanel(new BorderLayout());
 
 		JPanel btnPnl = new JPanel();
 		argBtn = new JButton("Arguments");
 		attribBtn = new JButton("Attributes");
 		spcifyExecBtn = new JButton("Executables");
 		argBtn.addActionListener(this);
 		attribBtn.addActionListener(this);
 		spcifyExecBtn.addActionListener(this);
 		btnPnl.add(argBtn);
 		btnPnl.add(attribBtn);
 		btnPnl.add(spcifyExecBtn);
 
 		pnl1 = new JPanel(new GridLayout(3, 2));
 		JLabel pLbl = new JLabel("Program Name: ", JLabel.RIGHT);
 		jcBox = new JComboBox();
 		jcBox.addItem("SORCER - Proth");
 		pnl1.add(pLbl);
 		pnl1.add(jcBox);
 
 		// pnl2 = new JPanel(new GridLayout(1,2));
 		JLabel jsLbl = new JLabel("Job Size: ", JLabel.RIGHT);
 		jSizeTfld = new JTextField("5", 10);
 		pnl1.add(jsLbl);
 		pnl1.add(jSizeTfld);
 
 		// pnl3 = new JPanel(new GridLayout(1,2));
 		JLabel ntfyLbl = new JLabel("Notify: ", JLabel.RIGHT);
 		notifyTfld = new JTextField(20);
 		pnl1.add(ntfyLbl);
 		pnl1.add(notifyTfld);
 
 		pnl4 = new JPanel(new FlowLayout());
 		runBtn = new JButton("Run");
 		clearBtn = new JButton("Clear");
 		runBtn.addActionListener(this);
 		clearBtn.addActionListener(this);
 		pnl4.add(runBtn);
 		pnl4.add(clearBtn);
 
 		JPanel centerPnl = new JPanel();
 		centerPnl.setLayout(new BoxLayout(centerPnl, BoxLayout.Y_AXIS));
 		centerPnl.add(new JLabel(" "));
 		centerPnl.add(pnl1);
 		// centerPnl.add(pnl2);
 		// centerPnl.add(pnl3);
 		centerPnl.add(btnPnl);
 		// centerPnl.add(pnl4);
 
 		JLabel paramLbl = new JLabel("Set Application Parameters",
 				JLabel.CENTER);
 		Font pFnt = paramLbl.getFont();
 		int x = pFnt.getSize();
 		pFnt = pFnt.deriveFont((float) (x + 4));
 		paramLbl.setFont(pFnt);
 		paramPnl.add(paramLbl, BorderLayout.NORTH);
 		paramPnl.add(centerPnl, BorderLayout.CENTER);
 		paramPnl.add(pnl4, BorderLayout.SOUTH);
 
 		return paramPnl;
 	}
 
 	private JPanel getInputFilePnl() {
 		JPanel jpnl = new JPanel();
 		jpnl.add(new JLabel("Input File :"));
 		jpnl.add(browseTfld = new JTextField(20));
 		JButton browse = new JButton("Browse");
 		browse.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				JFileChooser chooser = new JFileChooser();
 				int returnVal = chooser.showOpenDialog(browseTfld);
 				if (returnVal == JFileChooser.APPROVE_OPTION)
 					browseTfld.setText(chooser.getSelectedFile()
 							.getAbsolutePath());
 			}
 		});
 
 		JButton insert = new JButton("Insert");
 		insert.addActionListener(this);
 
 		jpnl.add(browse);
 		jpnl.add(insert);
 		return jpnl;
 	}
 
 	private JPanel getFileStoreInputUI() {
 		JPanel jpnl = new JPanel(new BorderLayout());
 
 		JPanel centerPnl = new JPanel(new BorderLayout());
 		JPanel centerLeftPnl = new JPanel(new GridLayout(2, 1));
 		JPanel centerRightPnl = new JPanel(new GridLayout(2, 1));
 
 		centerLeftPnl.add(new JLabel("Folder: ", JLabel.CENTER));
 		centerLeftPnl.add(new JLabel("File: ", JLabel.CENTER));
 
 		centerRightPnl.add(fsInFolderTfld = new JTextField(20));
 		centerRightPnl.add(fsInFileTfld = new JTextField(20));
 
 		centerPnl.add(centerLeftPnl, BorderLayout.WEST);
 		centerPnl.add(centerRightPnl, BorderLayout.CENTER);
 
 		jpnl.add(new JLabel("File Store Input", JLabel.CENTER),
 				BorderLayout.NORTH);
 		jpnl.add(centerPnl, BorderLayout.CENTER);
 		jpnl.add(new Label("  "), BorderLayout.WEST);
 		jpnl.add(new Label("  "), BorderLayout.EAST);
 		jpnl.setBorder(javax.swing.border.LineBorder.createGrayLineBorder());
 		return jpnl;
 	}
 
 	private JPanel getFileStoreOutputUI() {
 		JPanel jpnl = new JPanel(new BorderLayout());
 
 		JPanel centerPnl = new JPanel(new BorderLayout());
 		JPanel centerLeftPnl = new JPanel(new GridLayout(2, 1));
 		JPanel centerRightPnl = new JPanel(new GridLayout(2, 1));
 		centerLeftPnl.add(new JLabel("Folder: ", JLabel.CENTER));
 		centerLeftPnl.add(new JLabel("File: ", JLabel.CENTER));
 		centerRightPnl.add(fsOutFolderTfld = new JTextField(20));
 		centerRightPnl.add(fsOutFileTfld = new JTextField(20));
 
 		centerPnl.add(centerLeftPnl, BorderLayout.WEST);
 		centerPnl.add(centerRightPnl, BorderLayout.CENTER);
 
 		jpnl.add(new JLabel("File Store Output", JLabel.CENTER),
 				BorderLayout.NORTH);
 		jpnl.add(centerPnl, BorderLayout.CENTER);
 		jpnl.add(new Label("  "), BorderLayout.WEST);
 		jpnl.add(new Label("  "), BorderLayout.EAST);
 		jpnl.setBorder(javax.swing.border.LineBorder.createGrayLineBorder());
 		return jpnl;
 	}
 
 	private JPanel getInputListUI() {
 		JPanel jpnl = new JPanel(new BorderLayout());
 		jpnl.add(new JLabel("Input List", JLabel.CENTER), BorderLayout.NORTH);
 		jpnl.add((inListTarea = new JTextArea(10, 1)), BorderLayout.CENTER);
 		jpnl.setBorder(javax.swing.border.LineBorder.createGrayLineBorder());
 		return jpnl;
 	}
 
 	private JPanel getOutputListUI() {
 		JPanel jpnl = new JPanel(new BorderLayout());
 		jpnl.add(new JLabel("Output List", JLabel.CENTER), BorderLayout.NORTH);
 		jpnl.add((outListTarea = new JTextArea(10, 1)), BorderLayout.CENTER);
 		outListTarea.setEditable(false);
 		jpnl.setBorder(javax.swing.border.LineBorder.createGrayLineBorder());
 		return jpnl;
 	}
 
 	private JPanel getBottomPnl() {
 		JPanel bottomPnl = new JPanel(new BorderLayout());
 
 		JPanel pnl1 = new JPanel(new FlowLayout());
 		pnl1.add(new JLabel("Job Size:", JLabel.RIGHT));
 		pnl1.add(jSizeTfld = new JTextField(10));
 		pnl1.add(new JLabel("Notify:", JLabel.RIGHT));
 		pnl1.add(notifyTfld = new JTextField(20));
 
 		JPanel pnl2 = new JPanel(new FlowLayout());
 		// pnl2.add(fsBtn = new JButton("File Store"));
 		pnl2.add(runBtn = new JButton("Run"));
 		pnl2.add(clearBtn = new JButton("Clear"));
 
 		// fsBtn.addActionListener(this);
 		runBtn.addActionListener(this);
 		clearBtn.addActionListener(this);
 
 		bottomPnl.add(pnl1, BorderLayout.NORTH);
 		bottomPnl.add(pnl2, BorderLayout.SOUTH);
 
 		return bottomPnl;
 	}
 
 	// ----------------------------------BUILD GUI
 	// OVER------------------------------------
 
 	// ---------------------------------The meat Button
 	// Actions----------------------------
 	public void actionPerformed(ActionEvent ae) {
 		String cmd = ae.getActionCommand();
 		// debug("LOGGED SUBJECT ==== "+loggedSubject);
 		// if(loggedSubject!=null){
 		if ("Clear".equals(cmd)) {
 			clear();
 		} else if ("Run".equals(cmd)) {
 			int mode = findGUIMode();
 			try {
 				run1(mode);
 				System.out.println(" Object Outframe Created");
 			} catch (RemoteException re) {
 				re.printStackTrace();
 				JOptionPane.showConfirmDialog(this, re.getMessage(),
 						"Remote Exception", JOptionPane.CLOSED_OPTION);
 			}
 		} else if ("Insert".equals(cmd)) {
 			Vector v = parseInputFile();
 			browseTfld.setText("");
 			for (int i = 0; i < v.size(); i++) {
 				inListTarea.append(v.elementAt(i).toString());
 				inListTarea.append("\n");
 			}
 		} else if ("Executables".equals(cmd)) {
 			// Create a gui on the fly
 			System.out.println("Create Context.....");
 			// GridDispatcherCtxUI dCtxUI = new GridDispatcherCtxUI();
 			GridDispatcherExecUI dExecUI = new GridDispatcherExecUI();
 			// dCtxUI.show();
 			dExecUI.show();
 		} else if ("Arguments".equals(cmd)) {
 			// Create a gui on the fly
 			System.out.println("Arguments.....");
 			GridDispatcherArgUI dArgUI = new GridDispatcherArgUI();
 			dArgUI.pack();
 			dArgUI.show();
 		} else if ("Attributes".equals(cmd)) {
 			// Create gui on the fly
 			System.out.println("Attributes ....");
 			GridDispatcherAttribUI dAttribUI = new GridDispatcherAttribUI();
 			dAttribUI.pack();
 			dAttribUI.show();
 		}
 		// }
 		else {
 			try {
 				// debug(this+"===================================================
 				// loggedSubject was bull!! =============="+loggedSubject);
 				// getAuthUI();
 			} catch (Exception ex) {
 				System.out
 						.println("-------- Error in the Login part of GridDispatcherUI------------");
 				ex.printStackTrace();
 				JOptionPane.showMessageDialog(this, "Login Failed!\n\n"
 						+ ex.getStackTrace());
 			}
 		}
 	}
 
 	private void clear() {
 		browseTfld.setText("");
 		fsInFolderTfld.setText("");
 		fsInFileTfld.setText("");
 		fsOutFolderTfld.setText("");
 		fsOutFileTfld.setText("");
 		jSizeTfld.setText("");
 		notifyTfld.setText("");
 		inListTarea.setText("");
 		outListTarea.setText("");
 	}
 
 	final int FS_IN_FS_OUT = 0;
 
 	final int LIST_IN_FS_OUT = 1;
 
 	final int LIST_IN_LIST_OUT = 2;
 
 	private boolean isEmpty(javax.swing.text.JTextComponent tfld) {
 		// System.out.println("isEmpty called for "+tfld+ " : " + (
 		// (tfld.getText()==null) || "".equals(tfld.getText()) ));
 		return ((tfld.getText() == null) || "".equals(tfld.getText()));
 	}
 
 	private int findGUIMode() {
 		int mode;
 		if (!isEmpty(browseTfld) || !isEmpty(fsInFolderTfld)) {
 			if (isEmpty(fsInFolderTfld) || isEmpty(fsInFileTfld)
 					|| isEmpty(fsOutFolderTfld) || isEmpty(fsOutFileTfld)) {
 				JOptionPane.showConfirmDialog(this,
 						"Enter valid input and output for file store",
 						"Input Error", JOptionPane.OK_OPTION);
 				return -1;
 			} else
 				return FS_IN_FS_OUT;
 		} else if (!isEmpty(inListTarea)) {
 			if (isEmpty(fsOutFolderTfld) || isEmpty(fsOutFileTfld))
 				return LIST_IN_LIST_OUT;
 			else
 				return LIST_IN_FS_OUT;
 		} else {
 			JOptionPane.showConfirmDialog(this,
 					"Enter valid input and output for file store",
 					"Input Error", JOptionPane.OK_OPTION);
 			return -1;
 		}
 	}
 
 	private void freeze() {
 		/*
 		 * //Buttons unpressable //fsBtn.setEnabled(false);
 		 * runBtn.setEnabled(false); clearBtn.setEnabled(false); //Txtfld and
 		 * aread uneditable browseTfld.setEditable(false);
 		 * fsInFolderTfld.setEditable(false); fsInFileTfld.setEditable(false);
 		 * fsOutFolderTfld.setEditable(false); fsOutFileTfld.setEditable(false);
 		 * jSizeTfld.setEditable(false); notifyTfld.setEditable(false);
 		 * inListTarea.setEditable(false); outListTarea.setEditable(false);
 		 */
 	}
 
 	// ***Change
 	// Abhijit::: This method sets the subject inside the ctx of the client
 	// Needs to be put into the SecureSorcerUI
 	// The API should emphasise the use of this method if the client requires
 	// delegation, other wise client call may not succeed
 	// The control strategy is basically the service checks the ClientSubject,
 	// if null, it checks whether the ctx has a subject
 	// and then callsSubject.doAs on this
 	// Extention to check the validity of the delegation call, so each context
 	// shall contain the validity period to be specified in
 	// prepare-minimal.config
 	// service will check the validity and only then allow delegation
 	// Later use client signed approach by mike to allow delegation in the same
 	// way, only this time delegation checks ClientSubject, if null, it checks
 	// whether client has signed the servers ublic key and if he has allowed
 	// delegation, if delegation is allowed, finds the private key from context
 	// and delegates by calling Subject.doAs
 	// This will help since Client will now be authenticated on all services
 	// Or the ctx can just be sent ahead with servers credentials and finally
 	// when provider.service is called there the client subject is checked and
 	// AccessControl is performed
 	// The service method can be overridden and this may or will cause security
 	// issues
 
 	public void setSubject(Subject subj, Context ctx) throws ContextException {
 		ctx.putValue("subject", subj);
 		ctx.putValue("delegate", "yes");
 	}
 
 	// ----------------------- RUN BUTTON ACTION
 	// BEGINS-------------------------------
 	// public final ServiceContext ctx2;
 	public int setSubjectTries = 0;
 
 	private final void run1(int mode) throws RemoteException {
 		// AccessControlContext context = AccessController.getContext();
 		// Subject currentSubject = Subject.getSubject(context);
 		final int mode1 = mode;
 		if (mode == -1)
 			return;
 		if (mode == FS_IN_FS_OUT)
 			try {
 				if (!isEmpty(browseTfld))
 					uploadInputFile();
 			} catch (Exception ioe) {
 				ioe.printStackTrace();
 				JOptionPane.showConfirmDialog(this, ioe.getMessage(),
 						"File Upload Error", JOptionPane.CLOSED_OPTION);
 				return;
 			}
 		Context ctx = getServiceContext(mode);
 		/*
 		 * try{ //debug(this+">>> ------------------------------ [Setting
 		 * subject field of the ctx]"+loggedSubject); setSubject(loggedSubject,
 		 * ctx); //debug(this+">>>-------------------------------------[Getting
 		 * subject field of the ctx]"+ctx.getValue("subject"));
 		 * }catch(ContextException ex){ //debug(this+">>> Context Exception
 		 * occured"); ex.printStackTrace(); }
 		 */
 		final Context ctx1 = ctx;
 		NetSignature method;
 		try {
 			method = new NetSignature("computePrime",
 					GridDispatcherRemote.class);
 			final NetTask task = new NetTask("computePrime",
 					"computePrime", method);
 
 			// ***Abhijit:: SecureSorcerUI shall have a generic implementation
 			// to
 			// set the principal of the ctx
 			/*
 			 * //should pick out one of the various principals X500Principal
 			 * x500Principal = null; Iterator it =
 			 * loggedSubject.getPrincipals().iterator(); Object p = null;
 			 * //debug(" -------------------------------------Iterating
 			 * ---------------------------------"); while(it.hasNext()){
 			 * p=it.next(); System.out.println("Checking -----p="+p); if(p
 			 * instanceof X500Principal){ x500Principal = (X500Principal)p;
 			 * break; } } //debug("Abhijit:: ----- Principal to be put in CTX =
 			 * "+x500Principal+" ---------"); debug("Principal :
 			 * " + x500Principal + " calling on the service" + dispatcher1);
 			 * ctx.setPrincipal(new GAppPrincipal(x500Principal.getName()));
 			 * //this is an asyncronous call.
 			 * 
 			 * try{ ctx= (ServiceContext)Subject.doAsPrivileged(loggedSubject,
 			 * new PrivilegedExceptionAction() { public Object run() throws
 			 * Exception { return
 			 * ((ServiceTask)dispatcher1.service(task)).getContext(); //return
 			 * null; } }, null); //debug("callin the remote method on
 			 * "+dispatcher);
 			 * 
 			 * }catch (Exception ex){ System.out.println("Problem in the remote
 			 * call"); ex.printStackTrace();
 			 * JOptionPane.showMessageDialog(this,"Remote Call Failed"); }
 			 */
 			// isolated to avoid security
 
 			ctx = ((ServiceExertion) dispatcher1.service(task, null))
 					.getContext();
 		} catch (Exception ex) {
 			System.out.println("Problem in the remote call");
 			ex.printStackTrace();
 			JOptionPane.showMessageDialog(this, "Remote Call Failed");
 		}
 
 		if (mode == LIST_IN_LIST_OUT) {
 			freeze();
 		} else {
 			popupResultBrowser(ctx);
 			// clear();
 		}
 	}
 
 	// public class DoPriviledged() extends PriviledgedAction{
 	// D
 
 	private ServiceContext getServiceContext(int mode) {
 		ServiceContext ctx = new ServiceContext();
 		try {
 			if (mode == FS_IN_FS_OUT) {
 
 				GridDispatcherContextUtil.setInputFile(ctx, fsInFolderTfld
 						.getText()
 						+ "/" + fsInFileTfld.getText());
 
 				GridDispatcherContextUtil.setOutputFile(ctx, fsOutFolderTfld
 						.getText()
 						+ "/" + fsOutFileTfld.getText());
 			} else if (mode == LIST_IN_FS_OUT) {
 				String values = inListTarea.getText();
 				String[] paramValues = SorcerUtil.tokenize(values, "\n");
 				GridDispatcherContextUtil.setInputValues(ctx, paramValues);
 				GridDispatcherContextUtil.setOutputFile(ctx, fsOutFolderTfld
 						.getText()
 						+ "/" + fsOutFileTfld.getText());
 			} else {
 				// LIST_IN_LIST_OUT
 				// Freeze the GUI. No operation permitted as
 				// we need to display the ouput in GUI.
 				String values = inListTarea.getText();
 				String[] paramValues = SorcerUtil.tokenize(values, "\n");
 				GridDispatcherContextUtil.setInputValues(ctx, paramValues);
 
 				listener.notify(null);
 
 				GridDispatcherContextUtil.setCallback(ctx, listener);
 			}
 
 			GridDispatcherContextUtil.setJobSize(ctx, jSizeTfld.getText());
 			GridDispatcherContextUtil.setNotify(ctx, notifyTfld.getText());
 		} catch (ContextException e) {
 			e.printStackTrace();
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnknownEventException e) {
 			e.printStackTrace();
 		}
 		return ctx;
 	}
 
 	private void popupResultBrowser(Context ctx) {
 		JOptionPane.showConfirmDialog(this,
 				"Login to DocumentManaget to see your output at ", "Result ",
 				JOptionPane.OK_OPTION);
 	}
 
 	public void done() {
 		// clear();
 		// Buttons unpressable
 		// fsBtn.setEnabled(true);
 		// runBtn.setEnabled(true);
 		clearBtn.setEnabled(true);
 		// Txtfld and aread uneditable
 		browseTfld.setEditable(true);
 		fsInFolderTfld.setEditable(true);
 		fsInFileTfld.setEditable(true);
 		fsOutFolderTfld.setEditable(true);
 		fsOutFileTfld.setEditable(true);
 		jSizeTfld.setEditable(true);
 		notifyTfld.setEditable(true);
 		inListTarea.setEditable(true);
 	}
 
 	public void recievedOutput(String output) {
 		outListTarea.append("\n");
 		outListTarea.append(output);
 		runBtn.setEnabled(true);
 		outFrame.pack();
 		System.out.println(" Going to Outframe");
 		outFrame.show();
 	}
 
 	static SorcerPrincipal principal = new SorcerPrincipal();
 	static {
 		principal.setId("363");
 		principal.setName("sobol");
 		principal.setRole("root");
 		principal.setAccessClass(4);
 		principal.setExportControl(false);
 	}
 
 	private void uploadInputFile() throws Exception {
 		FileStorer rfs = dispatcher.getFileStorer();
 		DocumentDescriptor docDesc = new DocumentDescriptor();
 		docDesc.setPrincipal(principal);
 		docDesc.setFolderPath(fsInFolderTfld.getText());
 
 		System.out.println(">>> folder Name = " + fsInFolderTfld.getText()
 				+ ">>> file name  = " + fsInFileTfld.getText() + ">>>>>>");
 		docDesc.setDocumentName(fsInFileTfld.getText());
 
 		docDesc = rfs.getOutputDescriptor(docDesc);
 
 		((OutputStreamProxy) docDesc.out).write(new File(browseTfld.getText()));
 	}
 
 	public static final class DispatcherListener implements
 			RemoteEventListener, Serializable, Remote {
 		public transient GridDispatcherUI ui;
 
 		public DispatcherListener() {
 
 		}
 
 		public DispatcherListener(GridDispatcherUI pui) {
 			ui = pui;
 		}
 
 		public RemoteEventListener getListener() throws RemoteException {
 			BasicJeriExporter exp = new BasicJeriExporter(TcpServerEndpoint
 					.getInstance(0), new BasicILFactory(), true, true);
 			return (RemoteEventListener) exp.export(this);
 		}
 
 		public void notify(RemoteEvent event) throws RemoteException {
 			if (event == null) {
 				System.out
 						.println("GridDispatcherUI.java:notify(RemoteEvent)::"
 								+ "Remote Event is Null");
 				return;
 			}
 			try {
 				System.out
 						.println("GridDispatcherUI.java:notify(RemoteEvent)::"
 								+ "Remote Event obtained");
 				String output = (String) event.getSource();
 				if ("_DONE_".equals(output))
 					ui.done();
 				ui.recievedOutput(output);
 			} catch (Exception e) {
 				e.printStackTrace();
 				throw new RemoteException("Exception occured in Output", e);
 			}
 		}
 
 	}
 
 	class OutFrame extends JFrame implements ActionListener {
 		public OutFrame() {
 			super();
 			try {
 				setTitle("Output List");
 				outListTarea = new JTextArea(40, 40);
 				getContentPane().setLayout(new BorderLayout());
 				JButton okBtn = new JButton("Cancel");
 				JButton cancelBtn = new JButton("Done");
 				cancelBtn.addActionListener(this);
 				okBtn.addActionListener(this);
 				outListTarea.setEditable(false);
 				JPanel tmpPnl = new JPanel();
 				tmpPnl.add(cancelBtn);
 				// tmpPnl.add(okBtn);
 				getContentPane().add(new JScrollPane(outListTarea),
 						BorderLayout.CENTER);
 				getContentPane().add(tmpPnl, BorderLayout.SOUTH);
 				pack();
 			} catch (Exception e) {
 				System.out.println("Problem in Constructor");
 				e.printStackTrace();
 			}
 		}
 
 		public void actionPerformed(ActionEvent ae) {
 			String cmd = ae.getActionCommand();
 			if ("Done".equals(cmd)) {
 				setVisible(false);
 			}
 			if ("Ok".equals(cmd)) {
 				setVisible(false);
 			}
 		}
 	}
 
 }
 
 /*
  * if(currentSubject==null){ System.out.print("Abhijit:: SUBJECT WAS NULL IN THE
  * THREAD FOR RUN, calling subject.doAS()"+ setSubjectTries++);
  * if(setSubjectTries<4){ Subject.doAsPrivileged(loggedSubject, new
  * PrivilegedAction(){ public Object run(){ try{ int mode2=mode1; run1(mode2);
  * return null; }catch (Exception ex){ ex.printStackTrace(); return null; } } },
  * null); } return; } else{ System.out.println("The client subject has been set
  * to = " + currentSubject); }
  */
 
 // ctx = dispatcher.computePrime(ctx);
 // ctx = ((ServiceTask)dispatcher1.service(task)).getContext();
 // System.out.println("Calling Subject.doAs, the loggedSubject is =
 // "+loggedSubject);
 /*
  * ctx= (ServiceContext)Subject.doAs(loggedSubject, new
  * PrivilegedExceptionAction() { public Object run() throws Exception { return
  * dispatcher.computePrime(ctx1); //return null; } } );
  */
 /*
  * ctx= (ServiceContext)Subject.doAs(loggedSubject, new PrivilegedAction() {
  * public Object run(){ try{ dispatcher.computePrime(ctx1); return null; }catch
  * (Exception ex){ System.out.println("Remote Exception caught in
  * Subject.doAs"); ex.printStackTrace(); return null; } //return null; } } );
  */
 /*
  * Vector v = new Vector(); v.add(loggedSubject);
  * ServerContext.doWithServerContext(new Runnable(){ public void run(){ try{
  * dispatcher.computePrime(ctx1); }catch (RemoteException ex){
  * System.out.println("Remote Exception caught in doWithServerContext");
  * ex.printStackTrace(); } } }, (Collection)v);
  */
