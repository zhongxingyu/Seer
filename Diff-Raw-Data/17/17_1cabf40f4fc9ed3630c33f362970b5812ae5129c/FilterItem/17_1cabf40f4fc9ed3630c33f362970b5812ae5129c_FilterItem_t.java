 /*  Copyright (C) 2012  Nicholas Wright
 	
 	part of 'Aid', an imageboard downloader.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package filter;
 
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 
 import com.j256.ormlite.field.DataType;
 import com.j256.ormlite.field.DatabaseField;
 import com.j256.ormlite.table.DatabaseTable;
 
 /**
  * This class stores the information about a item in the filter.
  * Information include: location, state and reason.
  */
 @DatabaseTable(tableName="filter")
 public class FilterItem implements Serializable{
 	
 	private static final long serialVersionUID = -9148296011482486975L;
 
 	@DatabaseField(canBeNull=false)
 	private String board;
 	@DatabaseField(canBeNull=false)
 	private String reason;
 	@DatabaseField(id=true, canBeNull=false)
 	private String id;
 	private URL url;
 	@DatabaseField(canBeNull=false, dataType = DataType.ENUM_INTEGER, columnName="status")
 	private FilterState state;
 	@DatabaseField
 	private Date timestamp;
 	private String displayString;
 	
 	private void updateDiplayString(){
 		String urlStr = url.toString();
 		String format = "%1$-5s %2$-10s %3$-30s";
 		String pageNr = urlStr.substring(urlStr.lastIndexOf("/")+1,urlStr.length());
 		displayString = String.format(format, board,pageNr,reason);
 	}
 	
 	/**
 	 * This constructor is only intended for the DAO.
 	 */
 	public FilterItem() {}
 	
 	public FilterItem( URL url,String board, String reason, FilterState state){
 		this.board = board;
 		this.reason = reason;
 		this.url = url;
 		this.state = state;
 		this.id = url.toString();
 		updateDiplayString();
 	}
 	
 	public URL getUrl(){
 		if(url == null){
 			try {
 				url = new URL(id);
 			} catch (MalformedURLException e) {}
 		}
 		
 		return url;
 	}
 	
 	public void setState(FilterState state){
 		this.state = state;
 	}
 	
 	public FilterState getState(){
 		return this.state;
 	}
 	
 	public String getReason(){
 		return reason;
 	}
 	
 	public String getBoard(){
 		return board;
 	}
 	
 	public Date getTimestamp() {
 		return timestamp;
 	}
 
 	@Override
 	public String toString(){
 		return displayString;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if(!(obj instanceof FilterItem) )
 			return false;
 
 		FilterItem fi = (FilterItem)obj;
 		
 		if(! fi.getUrl().equals(this.url))
 			return false;
 		
 		if(fi.getState() != this.state)
 			return false;
 		
 		if(! fi.getReason().equals(this.reason))
 			return false;
 		
 		if(! fi.getBoard().equals(this.board))
 			return false;
 		
 		return true;
 	}
 }
