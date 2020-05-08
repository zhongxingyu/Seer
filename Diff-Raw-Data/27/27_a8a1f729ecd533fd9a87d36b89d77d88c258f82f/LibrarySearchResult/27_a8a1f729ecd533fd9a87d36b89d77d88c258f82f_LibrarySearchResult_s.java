 package house.neko.media.common;
 
 
 /**
  *
  * @author andy
  */
 public class LibrarySearchResult
 {
 	/**
 	 *
 	 */
 	public Object[][] results;
 	/**
 	 *
 	 */
 	public String[] columnNames;
 	/**
 	 *
 	 */
 	public Class[] columnTypes;
 
 	/**
 	 *
 	 * @param cn
 	 * @param ct
 	 * @param r
 	 */
	public LibrarySearchResult(String[] cn, Class[] ct, Object[][] r)
 	{
 		results = r != null ? r : new String[0][0];
 		columnNames = cn != null ? cn : new String[0];
 		columnTypes = ct != null ? ct : new Class[0];
 	}
 	
 	public String toString()
 	{
 		StringBuilder sb = new StringBuilder("LibrarySearchResult(");
 		if(results == null)
 		{
 			sb.append("null");
 		} else {
 			sb.append(Integer.toString(results.length));
 		}
 		sb.append(",");
 		if(columnNames == null)
 		{
 			sb.append("null");
 		} else {
 			sb.append(Integer.toString(columnNames.length));
 		}
 		sb.append(")");
 		return sb.toString();
 	}
 }
