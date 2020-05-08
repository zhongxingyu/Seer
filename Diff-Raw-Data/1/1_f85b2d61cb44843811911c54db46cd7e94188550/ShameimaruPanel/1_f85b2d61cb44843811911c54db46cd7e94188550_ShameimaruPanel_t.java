 package com.luzi82.shameimaruboard.desktop;
 
 import java.util.Collections;
 import java.util.LinkedList;
 
 import javax.swing.JPanel;
 
 import com.luzi82.shameimaruboard.ShameimaruBoardViewer;
 import com.luzi82.shameimaruboard.model.SbMessage;
 
 public class ShameimaruPanel extends JPanel implements ShameimaruBoardViewer {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -382174260676473361L;
 
 	final LinkedList<SbMessage> mMessageList = new LinkedList<>();
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void insertMessage(SbMessage aMessage) {
 		synchronized (mMessageList) {
 			mMessageList.add(aMessage);
 			Collections.sort(mMessageList);
 		}
 		repaint();
 	}
 
 	@Override
 	public void removeMessage(SbMessage aMessage) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
