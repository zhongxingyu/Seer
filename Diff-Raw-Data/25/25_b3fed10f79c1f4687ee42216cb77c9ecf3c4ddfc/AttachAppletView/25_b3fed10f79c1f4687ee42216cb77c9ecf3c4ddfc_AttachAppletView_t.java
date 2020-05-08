 package org.remus.serverNodes;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Map;
 
 import org.mpstore.Serializer;
 import org.remus.RemusInstance;
 import org.remus.work.RemusApplet;
 
 public class AttachAppletView implements BaseNode {
 
 	RemusApplet applet;
 	RemusInstance inst;

 	public AttachAppletView(RemusApplet applet, RemusInstance inst) {
 		this.applet = applet;
 		this.inst = inst;
 	}
 
 	@Override
 	public void doDelete(String name, Map params, String workerID)
	throws FileNotFoundException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void doGet(String name, Map params, String workerID,
 			Serializer serial, OutputStream os) throws FileNotFoundException {
		if ( name.length() == 0 ) {
			for ( String key : applet.getAttachStore().listKeys(applet.getPath(), inst.toString() ) ) {
				try {
					os.write( serial.dumps( key ).getBytes() );
					os.write("\n".getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
 			}
		} else {
			throw new FileNotFoundException();
 		}
 	}
 
 	@Override
 	public void doPut(String name, String workerID, Serializer serial,
 			InputStream is, OutputStream os) throws FileNotFoundException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void doSubmit(String name, String workerID, Serializer serial,
 			InputStream is, OutputStream os) throws FileNotFoundException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public BaseNode getChild(String name) {		
 		if ( applet.getAttachStore().hasKey(  applet.getPath(), inst.toString(), name ) ) {
 			return new AttachListView(applet.getAttachStore(), applet.getPath(), inst.toString(), name );
 		}
 		return null;
 	}
 
 }
