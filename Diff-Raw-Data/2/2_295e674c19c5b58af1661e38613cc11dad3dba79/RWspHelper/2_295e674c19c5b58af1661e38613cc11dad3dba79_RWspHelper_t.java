 package org.geworkbench.builtin.projects;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.rmi.RemoteException;
 import java.util.HashMap;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableCellRenderer;
 
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.APSerializable;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
 import org.geworkbench.builtin.projects.WorkspaceHandler.OpenTask;
 import org.geworkbench.builtin.projects.WorkspaceHandler.SaveTask;
 import org.geworkbench.util.ProgressDialog;
 import org.geworkbench.util.ProgressItem;
 import org.geworkbench.util.ProgressTask;
 
 /**
  * Remote workspace helper classes
  * @author mw2518
  * $Id$
  */
 public class RWspHelper {
 	private static ProgressDialog pdmodal = ProgressDialog.create(ProgressDialog.MODAL_TYPE);
 	private static ProgressDialog pdnonmodal = ProgressDialog.create(ProgressDialog.NONMODAL_TYPE);
 	private static final String USER_INFO_DELIMIETER = RWspHandler.USER_INFO_DELIMIETER;
 	private static WorkspaceHandler ws = new WorkspaceHandler();
 	
 	protected static class DetailTableModel extends AbstractTableModel
 	{
 		private static final long serialVersionUID = 2482447217249689117L;
 		protected String[][] data;
 		private String[] header;
 
 		public DetailTableModel(String[][] tabledata, String[] tableheader){
 			data = tabledata;
 			header = tableheader;
 		}
 		public int getColumnCount() {
 			return header.length;
 		}
 
 		public int getRowCount() {
 			return data.length;
 		}
 
 		public String getColumnName(int col) {
 			return header[col];
 		}
 
 		public Object getValueAt(int row, int col) {
 			return data[row][col];
 		}
 	}
 	
 	protected static class WspTableModel extends DetailTableModel
 	{
 		private static final long serialVersionUID = -5175211782386134949L;
 
 		public WspTableModel(String[][] tabledata, String[] tableheader){
 			super(tabledata, tableheader);
 		}
 
 		public Object getValueAt(int row, int col) {
 			if (col==RWspHandler.DirtyID || col==RWspHandler.SyncID)
 				return Boolean.valueOf(data[row][col]);
 			return data[row][col];
 		}
 
 		public Class<?> getColumnClass(int col) {
 			return (col==RWspHandler.DirtyID||col==RWspHandler.SyncID)?
 					Boolean.class:String.class;
 		}
 	}
 
 	private static final int titleID = 1, dirtyID = 2, selectID = 3;
 	private static class LockTableModel extends DetailTableModel
 	{
 		private static final long serialVersionUID = -5175211782386134949L;
 
 		public LockTableModel(String[][] tabledata, String[] tableheader){
 			super(tabledata, tableheader);
 		}
 		public boolean isCellEditable(int row, int col) {
 			if (col == selectID)	return true;
 			else	return false;
 		}
 		public void setValueAt(Object value, int row, int col) {
 			data[row][col] = Boolean.toString((Boolean)value);
 			if ((Boolean)value==true) releasebtn.setEnabled(true);
 			else {
 				releasebtn.setEnabled(false);
 				for(int i=0; i<data.length; i++){
 					if (Boolean.valueOf(data[i][col])==true)
 						releasebtn.setEnabled(true);
 				}
 			}
 			fireTableCellUpdated(row, col);
 		}
 		public Object getValueAt(int row, int col) {
 			if (col==dirtyID || col==selectID)
 				return Boolean.valueOf(data[row][col]);
 			return data[row][col];
 		}
 		public Class<?> getColumnClass(int col) {
 			return (col==dirtyID||col==selectID)?
 					Boolean.class:String.class;
 		}
 	}
 
 	protected static class ColorRenderer extends DefaultTableCellRenderer {
 		private static final long serialVersionUID = -8091519198660967004L;
 
 		public Component getTableCellRendererComponent(JTable table, Object value, 
 				boolean isSelected, boolean hasFocus, int row, int col) {
 			Component comp = super.getTableCellRendererComponent(table, value,
 					isSelected, hasFocus, row, col);
 
 			String s = table.getModel().getValueAt(row, RWspHandler.LkUsrID).toString();
 			if (s != null && !s.equals("")){
 				if (isSelected) comp.setForeground(Color.yellow);
 				else            comp.setForeground(Color.red);
 			} else {
 				if (isSelected) comp.setForeground(Color.white);
 				else            comp.setForeground(null);
 			}
 			return comp;
 		}
 	}
 
 	protected static class AccessRemoteTask extends ProgressTask<HashMap<String, String>, Void> {
 		protected int id = 0;
 		AccessRemoteTask(int pbtype, String message, int id) {
 			super(pbtype, message);
 			this.id = id;
 		}
 		@Override
 		protected HashMap<String, String> doInBackground() {
 			HashMap<String, String> res = new HashMap<String, String>();
 			try {
 				res = DownloadClient.getSavedWorkspaceStatus("STATUS", id, RWspHandler.userInfo.split(USER_INFO_DELIMIETER)[0]);
 			} catch (Exception e1) { 
 				if (e1.getMessage().equals("Connection refused: connect")){
 					JOptionPane.showMessageDialog(null, e1.getMessage()+".\n\n"+
 					"GeWorkbench cannot connect to remote workspace server.\n" +
 					"It is possible that changes made to the local workspace copy may not be transferable to the server.\n"+
 					"When internet connection becomes available it will check with the server and update the status of the remote workspace.\n",
 					"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
 					res.put("ACCESS", "NoConnection");
 				}
     			return null;
 			}
 
 			return res;
 		}
 
 		protected HashMap<String, String> getResult(){
 			HashMap<String, String> res = new HashMap<String, String>();
 			try {
 				res = get();
 			} catch (ExecutionException e) {
 				e.getCause().printStackTrace();
 				JOptionPane.showMessageDialog(null,
 						"Exception: could not retrieve user access for remote workspace "+id,
 						"Retrieve User Access Error", JOptionPane.ERROR_MESSAGE);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 				JOptionPane.showMessageDialog(null,
 						"Exception: could not retrieve user access for remote workspace "+id+"\n"+
 						"It is possible that changes made to the local workspace copy may not be transferable to the server.\n"+e,
 						"Retrieve User Access Error", JOptionPane.ERROR_MESSAGE);
 			} finally {
 				pdnonmodal.removeTask(this);
 			}
 			return res;
 		}
 
 		@Override
 		protected void done(){
 			if (isCancelled()){
 				pdnonmodal.removeTask(this);
 				return;
 			}
 			HashMap<String, String> res = getResult();
 
 			if (res == null)
 				JOptionPane.showMessageDialog(null, "Could not retrieve user access for remote workspace "+id);
 			else if (res.get("ACCESS").equals("read"))
 				JOptionPane.showMessageDialog(null, 
 						"User access to the workspace is read only. Changes made to the local copy will not be transferable to the server.");
 			else {
 				boolean lock = Boolean.valueOf(res.get("LOCK"));
 				String lastsync = res.get("LASTSYNC");
 				if (lock == true) {
 					String lockuname = res.get("LOCKFNAME")+" "+res.get("LOCKLNAME");
 					//String lockuser = res.get("LOCKUSER");
 					//check lock owner or not?
 					//if (!lockuser.equals(RWspHandler.userInfo.split(USER_INFO_DELIMIETER)[0])) {
 						if (RWspHandler.checkoutstr.equals(lastsync)) {
 							JOptionPane.showMessageDialog(null, 
 									"Local workspace is STILL IN SYNC with the copy at the server,\n"+
 									"but the remote workspace is currently locked by user "+lockuname+".\n"+
 									"If you proceed with making changes to the local copy,\n"+
 									"you MAY NOT be able to synchronize them with the server.");
 						} else {
 							Object[] options = {"Download latest", "Keep out of sync"};
 							int n = JOptionPane.showOptionDialog(null, 
 									"Local workspace is OUT OF SYNC with the copy at the server,\n"+
 									"the remote workspace is currently locked by user "+lockuname+".\n"+
 									"If you proceed with making changes to the local copy,\n"+
 									"you WILL NOT be able to synchronize them with the server.\n"+
 									"Would you like to download the lastest version of the workspace from the server?",
 									"Out of sync options", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 
 									null, options, options[0]);
 							if (n == JOptionPane.YES_OPTION){
 								OpenRemoteTask openTask = new OpenRemoteTask(ProgressItem.INDETERMINATE_TYPE, 
 										"Remote workspace "+id+" is being retrieved and loaded.", id+".wsp", lastsync);
 								pdnonmodal.executeTask(openTask);
 							}
 						}
 					//}
 				} else {
 					//System.out.println(RWspHandler.checkoutstr+"; "+lastsync);
 					if (RWspHandler.checkoutstr.equals(lastsync)) {
 						String ret = "";
 						try{
 							ret = DownloadClient.modifySavedWorkspace("LOCK", String.valueOf(id), null, null, RWspHandler.userInfo);
 						}catch (Exception e){
 							e.printStackTrace();
 						}
 						if (ret!=null && ret.contains("success")) {
 							JOptionPane.showMessageDialog(null, 
 									"You have been given the lock to the workspace which allows you to make changes.\n"+
 									"Other users will not be able to modify the workspace unless they either\n"+
 									"synchronize their local copy with the server or manually release the lock.");
 						} else
 							JOptionPane.showMessageDialog(null, "Could not lock remote workspace "+id);
 
 					} else {
 						Object[] options = {"Download latest", "Keep out of sync"};
 						int n = JOptionPane.showOptionDialog(null, 
 								"Local workspace is OUT OF SYNC with the copy at the server.\n" +
 								"If you proceeds with making changes to the local copy,\n" +
 								"you WILL NOT be able to synchronize them with the server.\n" +
 								"Would you like to download the lastest version of the workspace from the server?",
 								"Out of sync options", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 
 								null, options, options[0]);
 						if (n == JOptionPane.YES_OPTION){
 							OpenRemoteTask openTask = new OpenRemoteTask(ProgressItem.INDETERMINATE_TYPE, 
 									"Remote workspace "+id+" is being retrieved and loaded.", id+".wsp", lastsync);
 							pdnonmodal.executeTask(openTask);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	protected static class CheckReleaseRemoteTask extends AccessRemoteTask {
 		CheckReleaseRemoteTask(int pbtype, String message, int id) {
 			super(pbtype, message, id);
 		}
 
 		@Override
 		protected void done(){
 			if (isCancelled()){
 				pdnonmodal.removeTask(this);
 				return;
 			}
 			HashMap<String, String> res = getResult();
 
 			boolean proceed = false;
 			if (res == null)
 				JOptionPane.showMessageDialog(null, "Could not retrieve user access for remote workspace "+id);
 			else {
 				boolean lock = Boolean.valueOf(res.get("LOCK"));
 				if (lock == true){
 					String lockuser = res.get("LOCKUSER");
 					if (lockuser.equals(RWspHandler.userInfo.split(USER_INFO_DELIMIETER)[0])) {
 						int t = JOptionPane.showConfirmDialog(null, "You are holding the lock to the workspace being closed. Do you want to release the lock?");
 						if (t == JOptionPane.YES_OPTION)
 							proceed = true;
 					}
 				}
 			}
 			if (proceed){
 				String ret = "";
 				try{
 					ret = DownloadClient.modifySavedWorkspace("RELEASE", String.valueOf(id), null, null, RWspHandler.userInfo);
 				}catch(Exception e){
 					JOptionPane.showMessageDialog(null, e.getMessage()+".\n\n"+
 			    			"GeWorkbench cannot release lock for remote workspace via axis2 web service.\n" +
 			    			"Please try again later or report the problem to geWorkbench support team.\n",
 			    			"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
 				}
 				if (ret!=null && ret.contains("success"))
 					JOptionPane.showMessageDialog(null, ret);
 				else
 					JOptionPane.showMessageDialog(null, "Could not release remote workspace "+id);
 			}
 		}
 	}
 	
 	protected static class CheckOpenRemoteTask extends AccessRemoteTask {
 		private String wsFilename = "", selectedco = "";
 		CheckOpenRemoteTask(int pbtype, String message, String filename, String selectedco, int id) {
 			super(pbtype, message, id);
 			this.wsFilename = filename;
 			this.selectedco = selectedco;
 		}
 
 		@Override
 		protected void done(){
 			if (isCancelled()){
 				pdnonmodal.removeTask(this);
 				return;
 			}
 			HashMap<String, String> res = getResult();
 
 			boolean proceed = false;
 			if (res == null)
 				JOptionPane.showMessageDialog(null, "Could not retrieve user access for remote workspace "+id);
 			else {
 				if (res.get("ACCESS").equals("read")) {
 					int t = JOptionPane.showConfirmDialog(null, 
 							"You are downloading a workspace that you only have \"read\" rights.\n"+
 							"If you make changes to it, you will not be able to synchronize with the server.\n"+
 							"Do you wish to proceed?\n");
 					if (t == JOptionPane.YES_OPTION)
 						proceed = true;
 				} else {
 					boolean lock = Boolean.valueOf(res.get("LOCK"));
 					if (lock == true){
 						String lockuser = res.get("LOCKUSER");
 						String lockuname = res.get("LOCKFNAME")+" "+res.get("LOCKLNAME");
 						if (lockuser.equals(RWspHandler.userInfo.split(USER_INFO_DELIMIETER)[0])) {
 							proceed = true;
 						} else {
 							int t = JOptionPane.showConfirmDialog(null, 
 									"You are downloading a workspace that is locked by "+lockuname+".\n"+
 									"Do you wish to proceed?\n");
 							if (t == JOptionPane.YES_OPTION)
 								proceed = true;
 						}
 					} else {
 						proceed = true;
 					}
 				}
 			}
 			if (proceed){
 				String localwspname = DownloadClient.wsprenames.get(id);
 				if (localwspname!=null && !localwspname.equals("")){
 					int n = JOptionPane.showConfirmDialog(null, 
 							"There is a local copy of the workspace being opened. Do you want to overwrite your local copy?",
 							"Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 					if (n == JOptionPane.CANCEL_OPTION || n == JOptionPane.NO_OPTION)
 						return;
 				}
 
 				OpenRemoteTask openTask = new OpenRemoteTask(ProgressItem.INDETERMINATE_TYPE, 
 						"Remote workspace "+wsFilename+" is being retrieved and loaded.", wsFilename, selectedco);
 				pdnonmodal.executeTask(openTask);
 			}
 		}
 	}
 	
 	private static class OpenRemoteTask extends OpenTask {
 		private String lastSync = "";
 		OpenRemoteTask(int pbtype, String message, String filename, String selectedco) {
 			ws.super(pbtype, message, filename);
 			lastSync = selectedco;
 		}
 		@Override
 		protected Void doInBackground() throws Exception {
 			String dldwspname = "";
 			try {
 				dldwspname = DownloadClient.getSavedWorkspace(filename);
 			} catch (Exception e1) {
 				JOptionPane.showMessageDialog(null, e1.getMessage()+".\n\n"+
     					"Exception: could not retrieve remote workspace "+filename,
     					"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
 				cancel(true);
     			return null;
 			}
 			DownloadClient.cleanCache();
 
 			filename = dldwspname;
 			super.doInBackground();
 
 			return null;
 		}
 		@Override
 		protected void done(){
 			super.done();
 			RWspHandler.checkoutstr = lastSync;
 			RWspHandler.dirty = false;
 			RWspHandler.lastchange = "";
 		}
 	}
 
 	protected static class CheckUpdateRemoteTask extends AccessRemoteTask {
 		private String filename = "";
 		private boolean terminating;
 		CheckUpdateRemoteTask(int pbtype, String message, String filename, boolean terminating, int id) {
 			super(pbtype, message, id);
 			this.filename = filename;
 			this.terminating = terminating;
 		}
 
 		@Override
 		protected void done(){
 			if (isCancelled()){
 				pdnonmodal.removeTask(this);
 				return;
 			}
 			HashMap<String, String> res = getResult();
 
 			boolean proceed = false;
 			if (res == null)
 				JOptionPane.showMessageDialog(null, "Could not retrieve user access for remote workspace "+id);
 			else if (res.get("ACCESS").equals("read"))
 				JOptionPane.showMessageDialog(null, 
 						"User access to the workspace is read only. Local copy cannot be synchronized with the server.");
 			else {
 				boolean lock = Boolean.valueOf(res.get("LOCK"));
 				String lastsync = res.get("LASTSYNC");
 				if (lock == true) {
 					String lockuser = res.get("LOCKUSER");
 					String lockuname = res.get("LOCKFNAME")+" "+res.get("LOCKLNAME");
 					if (!lockuser.equals(RWspHandler.userInfo.split(USER_INFO_DELIMIETER)[0])) {
 						if (RWspHandler.checkoutstr.equals(lastsync)) {
 							JOptionPane.showMessageDialog(null, 
 									"Local workspace is STILL IN SYNC with the copy at the server,\n"+
 									"but the remote workspace is currently locked by another user "+lockuname+".\n"+
 									"Please contact "+lockuname+" and ask them to release their lock,\n"+
 									"then you can check in your local copy.");
 						} else {
 							JOptionPane.showMessageDialog(null, 
 									"Local workspace is OUT OF SYNC with the copy at the server,\n"+
 									"It IS NOT possible to synchronize with the server.");
 						}
 					} else {
 						proceed = true;
 					}
 				} else {
 					//System.out.println(RWspHandler.checkoutstr+"; "+lastsync);
 					if (RWspHandler.checkoutstr.equals(lastsync)) {
 						String ret = "";
 						try{
 							ret = DownloadClient.modifySavedWorkspace("LOCK", String.valueOf(id), null, null, RWspHandler.userInfo);
 						}catch (Exception e){
 							e.printStackTrace();
 						}
 						if (ret!=null && ret.contains("success")) {
 							proceed = true;
 						} else
 							JOptionPane.showMessageDialog(null, "Could not lock remote workspace "+id);
 
 					} else {
 						JOptionPane.showMessageDialog(null, 
 								"Local workspace is OUT OF SYNC with the copy at the server.\n" +
 								"It IS NOT possible to synchronize with the server.");
 					}
 				}
 			}
 			if (proceed){
 				UpdateRemoteTask saveTask = new UpdateRemoteTask(ProgressItem.INDETERMINATE_TYPE, 
 						"Remote workspace "+id+" is being saved and uploaded.", filename, 
 						terminating, "SVRTIME");
 				pdmodal.executeTask(saveTask);
 			}
 		}
 	}
 
 	private static class UpdateRemoteTask extends SaveTask {
 		protected String type = "SVRTIME";
 		UpdateRemoteTask(int pbtype, String message, String filename, boolean terminating, String type) {
 			ws.super(pbtype, message, filename, terminating);
 			this.type = type;
 		}
 		@Override
 		protected Void doInBackground() throws FileNotFoundException, IOException {
 			//get server time as checkoutstr
 			try {
 				RWspHandler.checkoutstr = UploadClient.transferFile(null, type, null, null, String.valueOf(RWspHandler.wspId), RWspHandler.checkoutstr, RWspHandler.userInfo);
 			} catch (RemoteException e1) {
 				JOptionPane.showMessageDialog(null, e1.getMessage()+".\n\n"+
 	    				"GeWorkbench cannot upload to remote workspace via axis2 web service.\n" +
 	    				"Please try again later or report the problem to geWorkbench support team.\n",
 	    				"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
 				return null;
 			}
 			RWspHandler.dirty = false;
 			//save wsp
 			super.doInBackground();
 			//upload saved wsp file
 			try {
 				UploadClient.transferFile(new File(filename), "UPDATE", null, null, String.valueOf(RWspHandler.wspId), null, RWspHandler.userInfo);
 			} catch (RemoteException e1) {
 				JOptionPane.showMessageDialog(null, e1.getMessage()+".\n\n"+
     					"GeWorkbench cannot upload to remote workspace via axis2 web service.\n" +
     					"Please try again later or report the problem to geWorkbench support team.\n",
     					"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
     			return null;
 			}
 
 			int a = JOptionPane.showConfirmDialog(null, "The workspace was successfully uploaded.\n"+
 					"You are the only one who can modify the uploaded workspace.\n" + "Do you wish to release the lock?", 
 					"Release Workspace Lock", JOptionPane.YES_NO_OPTION);
 			if (a == JOptionPane.NO_OPTION ||a == JOptionPane.CLOSED_OPTION)
 				return null;
 			String res = "";
 			try{
 				res = DownloadClient.modifySavedWorkspace("RELEASE", String.valueOf(RWspHandler.wspId), null, null, RWspHandler.userInfo);
 			}catch(Exception e1){
 				JOptionPane.showMessageDialog(null, e1.getMessage()+".\n\n"+
     					"GeWorkbench cannot release lock for remote workspace via axis2 web service.\n" +
     					"Please try again later or report the problem to geWorkbench support team.\n",
     					"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
     			return null;
 			}
 			if (res!=null && res.contains("success"))
 				JOptionPane.showMessageDialog(null, res);
 			else
 				JOptionPane.showMessageDialog(null, "Could not release remote workspace "+RWspHandler.wspId);
 			return null;
 		}
 	}
 	
 	protected static class UploadNewTask extends UpdateRemoteTask {
 		private String title = "";
 		private String desc = "";
 		UploadNewTask(int pbtype, String message, String title, String desc, boolean terminating) {
 			super(pbtype, message, "", terminating, "NEW");
 			this.title = title;
 			this.desc = desc;
 		}
 		@Override
 		protected Void doInBackground() throws FileNotFoundException, IOException {
 			String id = "0";
 			// get auto-increment id in workspace table
 			try {
 				id = UploadClient.transferFile(null, type, title, desc, id, null, RWspHandler.userInfo);
 			} catch (RemoteException e1) {
 				JOptionPane.showMessageDialog(null, e1.getMessage()+".\n\n"+
 	    				"GeWorkbench cannot upload to remote workspace via axis2 web service.\n" +
 	    				"Please try again later or report the problem to geWorkbench support team.\n",
 	    				"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
 				return null;
 			}
 			// upload wsp file with just created id, no lock/sync check
 			RWspHandler.wspId = Integer.valueOf(id);
 			filename = RWspHandler.wspdir+id+".wsp";
 			type = "NEWSVRTIME";
 			super.doInBackground();
 			return null;
 		}
 	}
 
 	protected static class RemoveRemoteTask extends ProgressTask<String, Void> {
 		private String filename="", localfname="";
 		RemoveRemoteTask(int pbtype, String message, String fname, String lfname) {
 			super(pbtype, message);
 			filename = fname;
 			localfname = lfname;
 		}
 		@Override
 		protected String doInBackground() throws FileNotFoundException, IOException {
 			String res = "";
 			try {
 				res = DownloadClient.modifySavedWorkspace("REMOVE", filename, null, null, RWspHandler.userInfo);
 			} catch (Exception e1) {
 				JOptionPane.showMessageDialog(null, e1.getMessage()+".\n\n"+
 				"GeWorkbench cannot remove remote workspace" +filename+".\n"+
 				"Please try again later or report the problem to geWorkbench support team.\n",
 				"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
     			return null;
 			}
 
 			File localfile = new File(RWspHandler.wspdir+localfname);
 			if (localfile.exists()){
 				boolean b = localfile.delete();
 				System.out.println("delete "+localfname+" returns "+b);
 				if (!b) localfile.deleteOnExit();
 			}
 
 			return res;
 		}
 		@Override
 		protected void done(){
 			if (isCancelled()){
 				pdnonmodal.removeTask(this);
 				return;
 			}
 			String res = "";
 			try {
 				res = get();
 			} catch (ExecutionException e) {
 				e.getCause().printStackTrace();
 				JOptionPane.showMessageDialog(null,
 						"Exception: could not remove remote workspace "+filename,
 						"Remove Remote Workspace Error", JOptionPane.ERROR_MESSAGE);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 				JOptionPane.showMessageDialog(null,
 						"Exception: could not remove remote workspace "+filename+"\n"+e,
 						"Remove Remote Workspace Error", JOptionPane.ERROR_MESSAGE);
 			} finally {
 				pdnonmodal.removeTask(this);
 			}
 			if (res!=null && res.contains("success")) {
 				JOptionPane.showMessageDialog(null, res);
 				RWspHandler.listDialog.dispose();
 				RWspHandler.wspId = 0;
 			} else
 				JOptionPane.showMessageDialog(null, "Could not remove remote workspace "+filename);
 		}
 	}
 	
 	private static JDialog lockDialog;
 	private static JTable table;
 	private static JButton releasebtn;
 	private static class LSLockTask extends ProgressTask<String[][], Void> {
 
 		LSLockTask(int pbtype, String message) {
 			super(pbtype, message);
 		}
 
 		@Override
 		protected String[][] doInBackground() throws FileNotFoundException, IOException {
 			String[][] res = null;
 			try {
 				res = DownloadClient.getLockedWorkspaceList(RWspHandler.userInfo);
 			} catch (Exception e1) {
 				JOptionPane.showMessageDialog(null, e1.getMessage()+".\n\n"+
 				"GeWorkbench cannot retrieve locked remote workspace list.\n"+
 				"Please try again later or report the problem to geWorkbench support team.\n",
 				"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
 			}
 			return res;
 		}
 
 		@Override
 		protected void done() {
 			pdmodal.removeTask(this);
 			if (isCancelled()) return;
 
 			String[][] res = null;
 			try{
 				res = this.get();
 			} catch (ExecutionException e){
 				JOptionPane.showMessageDialog(null,
 						"Could not create retrieve locked remote workspace list.\n "+e.getMessage(), 
 						"Error", JOptionPane.ERROR_MESSAGE);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			
 			if (res == null)
 				JOptionPane.showMessageDialog(null, "Could not retrieve locked remote workspace list");
 			else if (res.length > 0){
 				lockDialog = new JDialog();
 				lockDialog.setModal(true);
 				lockDialog.setTitle("Remote workspaces locked by current user");
 				lockDialog.setLayout(new BorderLayout());
 				
 				table = new JTable(new LockTableModel(res, DownloadClient.collock));
 			    table.setPreferredScrollableViewportSize(table.getPreferredSize());
 				lockDialog.add(new javax.swing.JScrollPane(table), BorderLayout.CENTER);
 				JPanel btnpanel = new JPanel();
 				JButton selectallbtn = new JButton("Select All");
 				releasebtn = new JButton("Release");
 				releasebtn.setEnabled(false);
 				JButton cancelbtn = new JButton("Cancel");
 				btnpanel.add(selectallbtn);
 				selectallbtn.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent e){
 						for (int i = 0; i < table.getRowCount(); i++){
 							table.setValueAt(true, i, selectID);
 						}
 					}
 				});
 				btnpanel.add(releasebtn);
 				releasebtn.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent e){
 						for (int i = 0; i < table.getRowCount(); i++){
 							if ((Boolean)table.getValueAt(i, selectID)==true){
 								String id = table.getValueAt(i, 0).toString();
 								String ret = "";
 								if ((Boolean)table.getValueAt(i, dirtyID)==true){
 									Object[] options = {"Synchronize before release", "Release without synchronization", "Cancel"};
 									int t = JOptionPane.showOptionDialog(null, 
 											"Do you wish to synchronize the local copy of workspace '"
 											+table.getValueAt(i, titleID)+"' with the server before releasing the lock;\n"
 											+" or release the lock without synchronizing?", "Synchronize before release?", 
 											JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
 									if (t==JOptionPane.YES_OPTION){
 										//synchronize means upload local wsp, not download remote wsp
 										new RWspHandler().getUserInfo();
 										if (RWspHandler.userInfo == null) return;
										if (RWspHandler.userInfo.trim().length()==0) {
 											JOptionPane.showMessageDialog(null,
 															"Please make sure you entered valid username and password",
 															"Invalid User Account", JOptionPane.ERROR_MESSAGE);
 											return;
 										}
 										String fname = id+".wsp";
 										String rename = DownloadClient.wsprenames.get(Integer.valueOf(id));
 										if (rename!=null) fname=rename;
 
 										UpdateRemoteNoOpenTask updateTask = new UpdateRemoteNoOpenTask(ProgressItem.INDETERMINATE_TYPE,
 												"Workspace is being synchronized.", RWspHandler.wspdir+fname);
 										pdmodal.executeTask(updateTask);
 
 									} else if (t==JOptionPane.CANCEL_OPTION || t==JOptionPane.CLOSED_OPTION){
 										return;
 									}
 								}
 								try{
 									ret = DownloadClient.modifySavedWorkspace("RELEASE", String.valueOf(id), null, null, RWspHandler.userInfo);
 								}catch(Exception ex){
 									JOptionPane.showMessageDialog(null, ex.getMessage()+".\n\n"+
 							    			"GeWorkbench cannot release lock for remote workspace via axis2 web service.\n" +
 							    			"Please try again later or report the problem to geWorkbench support team.\n",
 							    			"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
 								}
 								if (ret==null || !ret.contains("success"))
 									JOptionPane.showMessageDialog(null, "Could not release remote workspace "+id);
 							}
 						}
 						lockDialog.dispose();
 					}
 				});
 				btnpanel.add(cancelbtn);
 				cancelbtn.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent e){
 						lockDialog.dispose();
 					}
 				});
 				lockDialog.add(btnpanel, BorderLayout.SOUTH);
 
 				lockDialog.pack();
 				lockDialog.setLocationRelativeTo(null);
 				lockDialog.setMinimumSize(new Dimension(400, 300));
 				lockDialog.setVisible(true);
 			}
 		}
 	}
 	protected static void listLock(){
 		if (RWspHandler.wspId > 0) RWspHandler.doSaveLocal(false);
 		if (RWspHandler.userInfo==null || RWspHandler.userInfo.equals("") 
 				|| RWspHandler.userInfo.equals("local")) return;
 
 		LSLockTask task = new LSLockTask(ProgressItem.INDETERMINATE_TYPE, "Locked workspace list is being retrieved.");
 		pdmodal.executeTask(task);
 
 	}
 
 	//update from local wsp file without populating prjpanel from savetree
 	private static class UpdateRemoteNoOpenTask extends ProgressTask<Void, Void>{
 		private String filename = null;
 		private SaveTree saveTree = null;
 		
 		UpdateRemoteNoOpenTask(int pbtype, String message, String filename) {
 			super(pbtype, message);
 			this.filename = filename;
 		}
 		@Override
 		protected Void doInBackground() {
 			FileInputStream in = null;
 			try {
 				in = new FileInputStream(filename);
 				ObjectInputStream s = new ObjectInputStream(in);
 				saveTree = (SaveTree) s.readObject();
 				APSerializable aps = (APSerializable) s.readObject();
 				AnnotationParser.setFromSerializable(aps);
 			} catch (Exception e) {
 				e.printStackTrace();
 				return null;
 			} finally {
 				try{
 					if(in!=null) in.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 
 			int wspId = saveTree.getWspId();
 			String checkoutstr = saveTree.getCheckout();
 			ObjectOutput s = null;
 			FileOutputStream f = null;
 			try{
 				checkoutstr = UploadClient.transferFile(null, "SVRTIME", null, null,
 						String.valueOf(wspId), checkoutstr, RWspHandler.userInfo);
 	
 				saveTree.setCheckout(checkoutstr);
 				saveTree.setDirty(false);
 				if (wspId==RWspHandler.wspId){
 					RWspHandler.dirty = false;
 					RWspHandler.checkoutstr = checkoutstr;
 				}
 	
 				f = new FileOutputStream(filename);
 				s = new ObjectOutputStream(f);
 				s.writeObject(saveTree);
 				APSerializable aps = AnnotationParser.getSerializable();
 				s.writeObject(aps);
 				s.flush();
 	
 				UploadClient.transferFile(new File(filename), "UPDATE", null, null, String.valueOf(wspId), null, RWspHandler.userInfo);
 			} catch (RemoteException e1) {
 				JOptionPane.showMessageDialog(null, e1.getMessage()+".\n\n"+
     					"GeWorkbench cannot upload to remote workspace via axis2 web service.\n" +
     					"Please try again later or report the problem to geWorkbench support team.\n",
     					"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
 			} catch (Exception e){
 				JOptionPane.showMessageDialog(null,	"Could not create workspace file for "
 						+e.getMessage()+". \nSave cancelled.", "Error", JOptionPane.ERROR_MESSAGE);
 				File file = new File(filename);
 				file.delete(); 
 			} finally {
 				try {
 					if (f!=null) f.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			return null;
 		}
 		@Override
 		protected void done() {
 			pdmodal.removeTask(this);
 			if (isCancelled()) return;
 	
 			try {
 				get();		
 			} catch (ExecutionException e) {
 				e.getCause().printStackTrace();
 				JOptionPane.showMessageDialog(null, "Couldn't synchronize workspace.",
 						"Synchronize Workspace Error", JOptionPane.ERROR_MESSAGE);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 				JOptionPane.showMessageDialog(null,	"Couldn't synchronize workspace.\n"+e,
 						"Synchronize Workspace Error", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 	}
 }
