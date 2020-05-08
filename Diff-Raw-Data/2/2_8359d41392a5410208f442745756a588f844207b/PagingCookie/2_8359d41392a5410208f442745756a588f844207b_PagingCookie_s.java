 package ch.sfdr.lacantina.dao;
 
 /**
  * 'Cookie' for paging. Holds the current position and the number of elements
  * in a list.
  * @author D.Ritz
  */
 public class PagingCookie
 {
	private static final int DEFAULT_PAGE_SIZE = 1;
 
 	private int offset;
 	private int limit = DEFAULT_PAGE_SIZE;
 	private int totalRows;
 	private int pageRows;
 
 	@Override
 	public String toString()
 	{
 		StringBuilder sb = new StringBuilder();
 		sb.append(offset).append(",");
 		sb.append(limit).append(",");
 		sb.append(totalRows).append(",");
 		sb.append(pageRows);
 		return sb.toString();
 	}
 
 	private static int string2int(String str)
 	{
 		try {
 			return Integer.parseInt(str);
 		} catch (NumberFormatException e) {
 			return 0;
 		}
 	}
 
 	/**
 	 * creates a paging cookie from String
 	 * @param str input string
 	 * @return PagingCookie
 	 */
 	public static PagingCookie fromString(String str)
 	{
 		PagingCookie pc = new PagingCookie();
 		String[] parts = str.split(",");
 		if (parts.length > 0)
 			pc.setOffset(string2int(parts[0]));
 		if (parts.length > 1)
 			pc.setLimit(string2int(parts[1]));
 		if (parts.length > 2)
 			pc.setTotalRows(string2int(parts[2]));
 		if (parts.length > 3)
 			pc.setPageRows(string2int(parts[3]));
 		return pc;
 	}
 
 	/**
 	 * sets the offset to the next page
 	 */
 	public void next()
 	{
 		offset += limit;
 	}
 
 	/**
 	 * sets the offset to the previous page
 	 * @return true if offset was updated
 	 */
 	public boolean prev()
 	{
 		offset -= limit;
 		if (offset < 0) {
 			offset = 0;
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * @return the offset
 	 */
 	public int getOffset()
 	{
 		return offset;
 	}
 
 	/**
 	 * @param offset the offset to set
 	 */
 	public void setOffset(int offset)
 	{
 		this.offset = offset;
 	}
 
 	/**
 	 * @return the limit
 	 */
 	public int getLimit()
 	{
 		return limit;
 	}
 
 	/**
 	 * @param limit the limit to set
 	 */
 	public void setLimit(int limit)
 	{
 		this.limit = limit;
 	}
 
 	/**
 	 * @return the totalRows
 	 */
 	public int getTotalRows()
 	{
 		return totalRows;
 	}
 
 	/**
 	 * @param totalRows the totalRows to set
 	 */
 	public void setTotalRows(int totalRows)
 	{
 		this.totalRows = totalRows;
 	}
 
 	/**
 	 * @return the pageRows
 	 */
 	public int getPageRows()
 	{
 		return pageRows;
 	}
 
 	/**
 	 * @param pageRows the pageRows to set
 	 */
 	public void setPageRows(int pageRows)
 	{
 		this.pageRows = pageRows;
 	}
 
 	/**
 	 * convenience helper for JSP: get the start of the range
 	 * @return offset + 1
 	 */
 	public int getRangeStart()
 	{
 		return offset + 1;
 	}
 
 	/**
 	 * convenience helper for JSP: get the end of the range
 	 * @return offset + pageRows
 	 */
 	public int getRangeEnd()
 	{
 		return offset + pageRows;
 	}
 
 	/**
 	 * convenience helper for JSP: returns a flag indicating if there are more pages
 	 * @return true if there are more pages
 	 */
 	public boolean getHasMoreRows()
 	{
 		return offset + pageRows < totalRows;
 	}
 }
