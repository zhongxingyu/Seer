 package org.fnppl.opensdx.common;
 
 import java.lang.reflect.Field;
 import java.util.Vector;
 
 import org.fnppl.opensdx.xml.ChildElementIterator;
 import org.fnppl.opensdx.xml.Element;
 import org.fnppl.opensdx.xml.XMLElementable;
 
 /*
  * Software license
  *
  * As far as this file or parts of this file is/are software, rather than documentation, this software-license applies / shall be applied.
  *  
  * This file is part of openSDX
  * openSDX is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * openSDX is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * and GNU General Public License along with openSDX.
  * If not, see <http://www.gnu.org/licenses/>.
  *      
  */
 
 /*
  * Documentation license
  * 
  * As far as this file or parts of this file is/are documentation, rather than software, this documentation-license applies / shall be applied.
  * 
  * This file is part of openSDX.
  * Permission is granted to copy, distribute and/or modify this document 
  * under the terms of the GNU Free Documentation License, Version 1.3 
  * or any later version published by the Free Software Foundation; 
  * with no Invariant Sections, no Front-Cover Texts, and no Back-Cover Texts. 
  * A copy of the license is included in the section entitled "GNU 
  * Free Documentation License" resp. in the file called "FDL.txt".
  * 
  */
 
 /**
  * 
  * @author Bertram Boedeker <bboedeker@gmx.de>
  * 
  */
 
 public class Bundle extends BusinessObject {
 
 	public static String KEY_NAME = "bundle";
 
 	private IDs ids;										//MUST
 	private BusinessStringItem displayname;					//MUST
 	private BusinessStringItem name;						//MUST
 	private BusinessStringItem version;						//MUST
 	private BusinessStringItem display_artistname;			//SHOULD
 	private BusinessCollection<Contributor> contributors;	//MUST
 	private BundleInformation information;					//MUST
 	private LicenseBasis license_basis;						//MUST
 	private LicenseSpecifics license_specifics;				//MUST
 	private ItemTags tags;									//COULD
 	private BusinessCollection<Item> items;					//SHOULD
 	private BusinessCollection<ItemFile> files;	 			//COULD
 
 	public static Bundle make(IDs ids, String displayname, String name, String version, String display_artistname, BundleInformation information, LicenseBasis license_basis, LicenseSpecifics license_specifics) {
 		Bundle bundle = new Bundle();
 		bundle.ids = ids;
 		bundle.displayname = new BusinessStringItem("displayname", displayname);
 		bundle.name = new BusinessStringItem("name", name);
 		bundle.version = new BusinessStringItem("version", version);
 		bundle.display_artistname = new BusinessStringItem("display_artistname", display_artistname);
 		bundle.contributors = new BusinessCollection<Contributor>() {
 			public String getKeyname() {
 				return "contributors";
 			}
 		};
 		bundle.information = information;
 		bundle.license_basis = license_basis;
 		bundle.license_specifics = license_specifics;
 		bundle.tags = null;
 		bundle.items = new BusinessCollection<Item>() {
 			public String getKeyname() {
 				return "items";
 			}
 		};
 		bundle.files = null;
 		return bundle;
 	}
 
 
 	public static Bundle fromBusinessObject(BusinessObject bo) {
 		if (bo==null) return null;
 		if (!bo.getKeyname().equals(KEY_NAME)) {
 			bo = bo.handleBusinessObject(KEY_NAME);
 		}
 		if (bo==null) return null;
 		final Bundle bundle = new Bundle();
 		bundle.initFromBusinessObject(bo);
 		
 		bundle.ids = IDs.fromBusinessObject(bo);
 		bundle.displayname = BusinessStringItem.fromBusinessObject(bo, "displayname");
 		bundle.name = BusinessStringItem.fromBusinessObject(bo, "name");
 		bundle.version = BusinessStringItem.fromBusinessObject(bo, "version");
 		bundle.display_artistname = BusinessStringItem.fromBusinessObject(bo, "display_artistname");
 		bundle.contributors =  new BusinessCollection<Contributor>() {
 			public String getKeyname() {
 				return "contributors";
 			}
 		};
 		new ChildElementIterator(bo, "contributors","contributor") {
 			public void processBusinessObject(BusinessObject bo) {
 				bundle.addContributor(Contributor.fromBusinessObject(bo));
 			}
 		};
 		
 		
 		bundle.information = BundleInformation.fromBusinessObject(bo);
 		bundle.license_basis = LicenseBasis.fromBusinessObject(bo);
 		bundle.license_specifics = LicenseSpecifics.fromBusinessObject(bo);
 		bundle.tags = ItemTags.fromBusinessObject(bo);
 		bundle.items =  null;
 		new ChildElementIterator(bo, "items","item") {
 			public void processBusinessObject(BusinessObject bo) {
 				bundle.addItem(Item.fromBusinessObject(bo));
 			}
 		};
 		bundle.files =  new BusinessCollection<ItemFile>() {
 			public String getKeyname() {
 				return "files";
 			}
 		};
 		new ChildElementIterator(bo, "files","file") {
 			public void processBusinessObject(BusinessObject bo) {
 				bundle.addFile(ItemFile.fromBusinessObject(bo));
 			}
 		};
 		
 		return bundle;
 	}
 	
 	public Bundle addFile(ItemFile file) {
 		if (files==null) {
 			files = new BusinessCollection<ItemFile>() {
 				public String getKeyname() {
 					return "files";
 				}
 			};
 		}
 		files.add(file);
 		return this;
 	}
 	
 	public void removeFile(int index) {
 		if (files==null) return;
 		files.remove(index);
 	}
 	
 	public int getFilesCount() {
 		if (files==null) return 0;
 		return files.size();
 	}
 	public ItemFile getFile(int index) {
 		if (files==null) return null;
 		if (index<0 || index>=files.size()) return null;
 		return files.get(index);
 	}
 
 	public Bundle addContributor(Contributor contributor) {
 		if (contributor == null) {
 			return this;
 		}
 		contributor.setAttribute("num", ""+(contributors.size()+1));
 		contributors.add(contributor);
 		return this;
 	}
 	
 	public void removeContributor(int index) {
 		if (contributors==null) return;
 		Contributor c = contributors.get(index);
 		if (c!=null) {
 			contributors.remove(index);
 			//also remove this contributor in all items in this bundle
 			for (int i=0;i<getItemsCount();i++) {
 				Item item = items.get(i);
 				for (int j=0;j<item.getContributorCount();j++) {
 					Contributor ic = item.getContributor(j);
 					if (	ic.getName().equals(c.getName())
 							&& ic.getType().equals(c.getType())
 						) {
 						item.removeContributor(j);
 						j--;
 					}
 					else {
 						// get Attribut "num" right
 						ic.setAttribute("num", ""+(j+1));
 					}
 				}
 			}
 		}
 	}
 	
 	public void removeContributor(Contributor c) {
 		if (contributors==null) return;
 		if (c!=null) {
 			int index = contributors.indexOf(c);
 			if (index>=0) contributors.remove(index);
 			
 			//also remove this contributor in all items in this bundle
 			for (int i=0;i<getItemsCount();i++) {
 				Item item = items.get(i);
 				for (int j=0;j<item.getContributorCount();j++) {
 					Contributor ic = item.getContributor(j);
 					if (	ic.getName().equals(c.getName())
 							&& ic.getType().equals(c.getType())
 						) {
 						item.removeContributor(j);
 						j--;
 					}
 					else {
 						// get Attribut "num" right
 						ic.setAttribute("num", ""+(j+1));
 					}					
 				}
 			}
 		}
 	}
 	
 	public void updateItemsContributors(Contributor updatedContrib, String oldName, String oldType) {
 		//update values in all item contributors
 		int itemCount = getItemsCount();
 		for (int j=0;j<itemCount;j++) {
 			Item it = getItem(j);
 			int contCout = it.getContributorCount();
 			for (int k=0;k<contCout;k++) {
				Contributor ic = it.getContributor(j);
				if (ic.getName().equals(oldName) && ic.getType().equals(oldType)) {
 					ic.name(updatedContrib.getName());
 					ic.type(updatedContrib.getType());
 					ic.year(updatedContrib.getYear());
 					ic.ids(updatedContrib.getIDs());
 					ic.www(updatedContrib.getWww());	
 				}
 			}
 		}
 	}
 	
 	public void moveItemUp(int ind) {
 		if (items==null) return;
 		items.moveUp(ind);
 	}
 	
 	public void moveItemDown(int ind) {
 		if (items==null) return;
 		items.moveDown(ind);
 	}
 	
 	public int getContributorCount() {
 		if (contributors==null) return 0;
 		return contributors.size();
 	}
 	
 	public Vector<Contributor> getAllContributors() {
 		Vector<Contributor> all = new Vector<Contributor>();
 		int num = 1;
 		for (int i=0;i<getContributorCount();i++) {
 			// set Attribute "num"
 			Contributor c = getContributor(i);
 			c.setAttribute("num", ""+num);
 			all.add(getContributor(i));
 			num++;
 		}
 		for (int j=0;j<getItemsCount();j++) {
 			Item item = getItem(j);
 			for (int i=0;i<item.getContributorCount();i++) {
 				Contributor c = item.getContributor(i);
 				boolean found = false;
 				for (int noC = 0; noC<all.size();noC++) {
 					Contributor a = all.get(noC);
 					if (a.getName().equals(c.getName()) && a.getType().equals(c.getType())) {
 						found = true;
 						break;
 					}
 				}
 				if (!found) {
 					c.on_sublevel_only(true);
 					c.setAttribute("num", ""+num);
 					all.add(c);
 					num++;
 				}
 			}
 		}
 		return all;
 	}
 	
 	public Bundle addItem(Item item) {
 		if (items==null) {
 			items = new BusinessCollection<Item>() {
 				public String getKeyname() {
 					return "items";
 				}
 			};
 		}
 		items.add(item);
 		return this;
 	}
 	
 	public void removeItem(int index) {
 		if (items==null) return;
 		items.remove(index);
 	}
 	
 	public int getItemsCount() {
 		if (items==null) return 0;
 		return items.size();
 	}
 	
 	public Item getItem(int index) {
 		if (items==null) return null;
 		return items.get(index);
 	}
 	
 	public Bundle tags(ItemTags tags) {
 		this.tags = tags;
 		return this;
 	}
 	
 	public ItemTags getTags() {
 		return tags;
 	}
 	
 //	public BusinessCollection<Item> getItems() {
 //		return items;
 //	}
 
 	public Bundle ids(IDs ids) {
 		this.ids = ids;
 		return this;
 	}
 
 	public Bundle displayname(String displayname) {
 		this.displayname = new BusinessStringItem("displayname", displayname);
 		return this;
 	}
 
 	public Bundle name(String name) {
 		this.name = new BusinessStringItem("name", name);
 		return this;
 	}
 
 	public Bundle version(String version) {
 		this.version = new BusinessStringItem("version", version);
 		return this;
 	}
 
 	public Bundle display_artistname(String display_artistname) {
 		this.display_artistname = new BusinessStringItem("display_artistname", display_artistname);
 		return this;
 	}
 
 	public Bundle information(BundleInformation information) {
 		this.information = information;
 		return this;
 	}
 
 	public Bundle license_basis(LicenseBasis license_basis) {
 		this.license_basis = license_basis;
 		return this;
 	}
 
 	public Bundle license_specifics(LicenseSpecifics license_specifics) {
 		this.license_specifics =license_specifics;
 		return this;
 	}
 
 
 	public IDs getIds() {
 		return ids;
 	}
 
 	public String getDisplayname() {
 		if (displayname==null) return null;
 		return displayname.getString();
 	}
 
 	public String getName() {
 		if (name==null) return null;
 		return name.getString();
 	}
 
 	public String getVersion() {
 		if (version==null) return null;
 		return version.getString();
 	}
 
 	public String getDisplay_artistname() {
 		if (display_artistname==null) return null;
 		return display_artistname.getString();
 	}
 
 	public Contributor getContributor(int index) {
 		if (contributors==null) return null;
 		if (index<0 || index>=contributors.size()) return null;
 		return contributors.get(index);
 	}
 
 
 	public BundleInformation getInformation() {
 		if (information==null) return null;
 		return information;
 	}
 
 	public LicenseBasis getLicense_basis() {
 		return license_basis;
 	}
 
 	public LicenseSpecifics getLicense_specifics() {
 		return license_specifics;
 	}
 	
 	public String getKeyname() {
 		return KEY_NAME;
 	}
 	
 	public Element toElement() {
 		Element resultElement = new Element(getKeyname());
 
 		Field[] fields = getDeclaredFieldsCache.get(this.getClass());
 		if(fields == null) {
 			fields = this.getClass().getDeclaredFields();
 			getDeclaredFieldsCache.put(this.getClass(), fields);
 
 			for(int i=0; i<fields.length; i++) {
 				fields[i].setAccessible(true);
 			}
 		}
 
 		for (Field f : fields) {
 			if (!f.getName().equals("this$0")) { //argg, watch out when directly using BusinessObjects
 				try {	
 					//System.out.println(f.getName());
 					Object thisFieldsObject = f.get(this);
 					if (thisFieldsObject == contributors) {
 						Element e = new Element(contributors.getKeyname());
 						for (int i=0;i<getContributorCount();i++) {
 							Contributor c = getContributor(i);
 							if (!c.getOnSubLevelOnly()) {
 								Element ce = c.toElement();
 								ce.setAttribute("num", ""+(i+1));
 								e.addContent(ce);
 							}
 						}
 						if (e!=null) {
 							resultElement.addContent(e);
 						}
 					}
 					else if (thisFieldsObject instanceof XMLElementable) {
 						Element e = ((XMLElementable)thisFieldsObject).toElement();
 						if (e!=null) {
 							resultElement.addContent(e);
 						}
 					}
 					else if (thisFieldsObject instanceof Vector<?>) {
 						Vector<?> vector = (Vector<?>)thisFieldsObject;
 						for (Object vectorsObject : vector) {
 							if (vectorsObject instanceof XMLElementable) {
 								Element e = ((XMLElementable)vectorsObject).toElement();
 								if (e!=null) {
 									resultElement.addContent(e);
 								}
 							}
 						}
 					}
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 		}
 		return resultElement;
 	}
 }
 
