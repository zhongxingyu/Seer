 package org.fehlis.applications.EpisodeRenamer.data;
 
 import java.util.Vector;
 import java.io.File;
 import javax.swing.ListModel;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 
public class FileListModel extends Vector<File> implements ListModel
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7713578554057491482L;
 	private Vector<ListDataListener> m_listeners;
 	
 	public FileListModel()
 	{
 		super();
 		m_listeners = new Vector<ListDataListener>();
 	}
 	
 	public int getSize() {
 		return size();
 	}
 
 	public Object getElementAt(int arg0) {
 		return elementAt( arg0 );
 	}
 
 	public void addListDataListener(ListDataListener arg0)
 	{
 		m_listeners.add( arg0 );
 	}
 
 	public void removeListDataListener(ListDataListener arg0)
 	{
 		m_listeners.remove( arg0 );
 	}
 	
 	public void updateListData( File[] list )
 	{
 //		int oldsize = size();
 		this.clear();
 //		notifyListeners( 0, 0, oldsize );
 		
 		for ( int i = 0; i < list.length; i++ )
 		{
 			this.add( list[i] );
 		}
 		
 		notifyListeners( 1, 0, size() );
 	}
 	
 	public void notifyListeners( int cmd, int index0, int index1 )
 	{
 		for( int i = 0; i < m_listeners.size(); i++ )
 		{
 			ListDataListener l = m_listeners.elementAt( i );
 			
 			if ( cmd == 0 )
 			{
 				l.intervalRemoved( new ListDataEvent( (Object) this, ListDataEvent.CONTENTS_CHANGED, index0, index1 ) );
 			}
 			else if ( cmd == 1 )
 			{
 				l.contentsChanged( new ListDataEvent( (Object) this, ListDataEvent.CONTENTS_CHANGED, index0, index1 ) );
 			}
 		}
 	}
 	
 }
