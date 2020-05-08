 package de.zigapeda.flowspring.data;
  
 import java.util.ArrayList;
 import java.util.List;
 
 import de.zigapeda.flowspring.Main;
 import de.zigapeda.flowspring.interfaces.TreeRow;
 
  
 public class DataNode {
 	
 	private static DataNode library;
 	private static DataNode search;
  
 	private DataNode parent;
     private TreeRow data;
     private List<DataNode> children;
  
     public DataNode(List<DataNode> children) {
     	this.data = new Dummy();
     	this.parent = null;
         this.children = children;
         
         if (this.children == null) {
             this.children = new ArrayList<DataNode>();
         }
     }
     
     public DataNode(TreeRow data, DataNode parent, List<DataNode> children) {
         this.data = data;
         this.parent = parent;
         this.children = children;
  
         if (this.children == null) {
             this.children = new ArrayList<DataNode>();
         }
     }
  
     public TreeRow getData() {
     	return this.data;
     }
     
     public void setData(TreeRow data) {
     	this.data = data;
     }
     
     public DataNode getParent() {
     	return this.parent;
     }
     
     public boolean isRoot() {
     	if(this.parent == null) {
     		return true;
     	}
     	return false;
     }
  
     public List<DataNode> getChildren() {
     	Integer type = null;
     	if(this.children.size() == 0) {
     		if(this.data.getType() != TreeRow.Title) {
     			if(Main.getWindow() != null) {
     				type = Main.getWindow().getControlllayout().getNextType(this.data.getType());
     				if(type != null) {
 	    				switch(type) {
 	    					case TreeRow.Title:
 	    						this.children = Title.getTitles(this.getStatement(),this);
 	    						break;
 	    					case TreeRow.Interpret:
 	    						this.children = Interpret.getInterprets(this.getStatement(),this);
 	    						break;
 	    					case TreeRow.Album:
 	    						this.children = Album.getAlbums(this.getStatement(),this);
 	    						break;
 	    					case TreeRow.Genre:
 	    						this.children = Genre.getGenres(this.getStatement(),this);
 	    						break;
 	    					case TreeRow.Track:
 	    						this.children = Track.getTracks(this.getStatement(),this);
 	    						break;
 	    					case TreeRow.Year:
 	    						this.children = Year.getYears(this.getStatement(),this);
 	    						break;
 	    					case TreeRow.Duration:
 	    						this.children = Duration.getDurations(this.getStatement(),this);
 	    						break;
 	    					case TreeRow.Comment:
 	    						this.children = Comment.getComments(this.getStatement(),this);
 	    						break;
 	    					case TreeRow.Rating:
 	    						this.children = Rating.getRatings(this.getStatement(),this);
 	    						break;
 	    					case TreeRow.Playcount:
 	    						this.children = Playcount.getPlaycounts(this.getStatement(),this);
 	    						break;
 	    				}
     				}
     			}
     		}
     	}
 		if(Main.getWindow() != null) {
 			if(type == null) {
 				sort(this.data.getType(), -1, this.children, Main.getWindow().getSortIndex(), Main.getWindow().getSortDirection());
 			} else {
 				sort(this.data.getType(), type, this.children, Main.getWindow().getSortIndex(), Main.getWindow().getSortDirection());
 			}
 		}
         return this.children;
     }
     
 	private void change(List<DataNode> list, int i) {
 		DataNode change = list.get(i);
 		list.set(i, list.get(i+1));
 		list.set(i+1,change);
 	}
 	
 	private void sort(int parenttype, int childtype, List<DataNode> list, int index, boolean direction) {
 //		getName();		0
 //		getArtist();	1
 //		getAlbum();		2
 //		getGenre();		3
 //		getTrack();		4
 //		getYear();		5
 //		getDuration();	6
 //		getComment();	7
 //		getRating();	8	
 //		getPlaycount();	9
 		for(int i = 0; i < list.size() - 1; i++) {
 			String s1 = null;
 			String s2 = null;
 			int i1 = 0;
 			int i2 = 0;
 			if(parenttype == TreeRow.Album && childtype == TreeRow.Title) {
 				i1 = Integer.valueOf(list.get(i).getData().getTrack());
 				i2 = Integer.valueOf(list.get(i+1).getData().getTrack());
 			} else {
 				switch(index) {
 					case 0:
 						s1 = list.get(i).getData().getName();
 						s2 = list.get(i+1).getData().getName();
 						break;
 					case 1:
 						s1 = list.get(i).getData().getArtist();
 						s2 = list.get(i+1).getData().getArtist();
 						break;
 					case 2:
 						s1 = list.get(i).getData().getAlbum();
 						s2 = list.get(i+1).getData().getAlbum();
 						break;
 					case 3:
 						s1 = list.get(i).getData().getGenre();
 						s2 = list.get(i+1).getData().getGenre();
 						break;
 					case 4:
 						s1 = list.get(i).getData().getTrack();
 						s2 = list.get(i+1).getData().getTrack();
 						break;
 					case 5:
 						s1 = list.get(i).getData().getYear();
 						s2 = list.get(i+1).getData().getYear();
 						break;
 					case 6:
 						i1 = list.get(i).getData().getDuration().intValue();
 						i2 = list.get(i+1).getData().getDuration().intValue();
 					case 7:
 						s1 = list.get(i).getData().getComment();
 						s2 = list.get(i+1).getData().getComment();
 						break;
 					case 8:
 						s1 = list.get(i).getData().getRating();
 						s2 = list.get(i+1).getData().getRating();
 						break;
 					case 9:
 						s1 = list.get(i).getData().getPlaycount();
 						s2 = list.get(i+1).getData().getPlaycount();
 						break;
 				}
 			}
 			if(s1 != null) {
 				if(direction == false) {
 					if(s1.compareToIgnoreCase(s2) > 0) {
 						change(list,i);
 						i = i - 2;
 						if(i < -1) {
 							i = -1;
 						}
 					}
 				} else {
 					if(s1.compareToIgnoreCase(s2) < 0) {
 						change(list,i);
 						i = i - 2;
 						if(i < -1) {
 							i = -1;
 						}
 					}
 				}
 			} else {
 				if(direction == false) {
 					if(i1 > i2) {
 						change(list,i);
 						i = i - 2;
 						if(i < -1) {
 							i = -1;
 						}
 					}
 				} else {
 					if(i1 < i2) {
 						change(list,i);
 						i = i - 2;
 						if(i < -1) {
 							i = -1;
 						}
 					}
 				}
 			}
 		}
 	}
 
     
     public void setChildren(List<DataNode> children) {
     	this.children = children;
     }
     
     private String getStatement() {
     	String stmt = "";
 		switch(this.data.getType()) {
 			case TreeRow.Interpret:
 				if(this.data.getId() != 0) {
 					stmt = " stk_int_id = " + String.valueOf(this.data.getId()) + " ";
 				} else {
 					stmt = " stk_int_id is null ";
 				}
 				break;
 			case TreeRow.Album:
 				if(this.data.getId() != 0) {
 					stmt = " stk_alb_id = " + String.valueOf(this.data.getId()) + " ";
 				} else {
 					stmt = " stk_alb_id is null ";
 				}
 				break;
 			case TreeRow.Genre:
 				if(this.data.getId() != 0) {
 					stmt = " stk_gre_id = " + String.valueOf(this.data.getId()) + " ";
 				} else {
 					stmt = " stk_gre_id is null ";
 				}
 				break;
 			case TreeRow.Track:
 				stmt = " stk_track = " + String.valueOf(this.data.getTrack()) + " ";
 				break;
 			case TreeRow.Year:
 				stmt = " stk_year = " + String.valueOf(this.data.getYear()) + " ";
 				break;
 			case TreeRow.Duration:
 				stmt = " stk_duration = " + String.valueOf(this.data.getInt()) + " ";
 				break;
 			case TreeRow.Comment:
 				if(this.data.getId() != 0) {
 					stmt = " stk_com_id = " + String.valueOf(this.data.getId()) + " ";
 				} else {
 					stmt = " stk_com_id is null ";
 				}
 				break;
 			case TreeRow.Rating:
 				stmt = " stk_rating = " + String.valueOf(this.data.getInt()) + " ";
 				break;
 			case TreeRow.Playcount:
 				stmt = " stk_playcount = " + String.valueOf(this.data.getPlaycount()) + " ";
 				break;
 		}
     	if(this.parent != null) {
     		stmt = this.parent.getStatement() + "and" + stmt;
     	}
     	return stmt;
     }
 
     public String toString() {
         return this.data.getName();
     }
     
     public static DataNode getLibrary() {
     	if(DataNode.library == null) {
     		DataNode.library = new DataNode(null);
     	}
     	return DataNode.library;
     }
 
 	public static DataNode getSearch() {
     	if(DataNode.search == null) {
     		DataNode.search = new DataNode(null);
     	}
 		return DataNode.search;
 	}
     
     public static void startSearch(String searchstring) {
     	DataNode.search.setChildren(Title.getTitlesBySearchstring(searchstring));
     }
     
     public static void endSearch() {
     	
     }
 
 	public static void refreshMedialib(Integer type) {
 		if(type != null) {
 			switch(type) {
 				case TreeRow.Title:
 					DataNode.getLibrary().setChildren(Title.getTitles(null,null));
 					break;
 				case TreeRow.Interpret:
 					DataNode.getLibrary().setChildren(Interpret.getInterprets(null,null));
 					break;
 				case TreeRow.Album:
 					DataNode.getLibrary().setChildren(Album.getAlbums(null,null));
 					break;
 				case TreeRow.Genre:
 					DataNode.getLibrary().setChildren(Genre.getGenres(null,null));
 					break;
 				case TreeRow.Track:
 					DataNode.getLibrary().setChildren(Track.getTracks(null,null));
 					break;
 				case TreeRow.Year:
 					DataNode.getLibrary().setChildren(Year.getYears(null,null));
 					break;
 				case TreeRow.Duration:
 					DataNode.getLibrary().setChildren(Duration.getDurations(null,null));
 					break;
 				case TreeRow.Comment:
 					DataNode.getLibrary().setChildren(Comment.getComments(null,null));
 					break;
 				case TreeRow.Rating:
 					DataNode.getLibrary().setChildren(Rating.getRatings(null,null));
 					break;
 				case TreeRow.Playcount:
 					DataNode.getLibrary().setChildren(Playcount.getPlaycounts(null,null));
 					break;
 			}
 		}
 	}
 }
