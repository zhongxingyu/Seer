 package prefwork.rating.datasource;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.XMLConfiguration;
 
 import prefwork.core.Utils;
 import prefwork.rating.Rating;
 import weka.core.Attribute;
 
 public class THMultiUserDataSource extends THDataSource{
 	protected boolean hasUserId = false;
 	protected boolean hasObjectId = false;
 	
 	protected void getData() {
 		try {
 			maxLoad = 10000;
 			BufferedReader in = new BufferedReader(
 					new FileReader(file + ".dat"));
 			String line;
 			int i = 0;
 			double max = Double.MIN_VALUE,min = Double.MAX_VALUE;
 			HashMap<Integer,List<Rating>> userRecords = new HashMap<Integer, List<Rating>>();
 			while ((line = in.readLine()) != null && i < maxLoad) {
 				Rating r = processOneLine(line, i);
 				if(r == null)
 					continue;
 				double rating = r.getRating();
 				if(rating>max)
 					max = rating;
 				if(rating<min)
 					min = rating;
 				if(!userRecords.containsKey(Utils.objectToInteger(r.getUserId())))
 						userRecords.put(Utils.objectToInteger(r.getUserId()), new ArrayList<Rating>());
 				
 				userRecords.get(Utils.objectToInteger(r.getUserId())).add(r);
 				i++;
 			}
 			in.close();
 			add = -min ;
 			coef = 4/(max-min);
 			this.userRecords = new Rating[userRecords.size()][];
 			int j = 0;
 			//Renumbering the users
 			for (Integer userId : userRecords.keySet()) {
 				this.userRecords[j]=new Rating[userRecords.get(userId).size()];
 				for (int j2 = 0; j2 < this.userRecords[j].length; j2++) {
 					this.userRecords[j][j2]=userRecords.get(userId).get(j2);		
 					this.userRecords[j][j2].setUserId(j);				
 				}				
 				j++;
 			}
 			userCount = userRecords.size();
 			shuffleInstances();
 			List<Double> classes = Utils.getList();
 				for (j = 0; j < this.userRecords.length; j++) {
 					for (int j2 = 0; j2 < this.userRecords[j].length; j2++) {
 						Rating rec = this.userRecords[j][j2];	
 						double r= Utils.objectToDouble(rec.get(2));
 						if(!classes.contains((r+add)*coef+1)){
 							classes.add((r+add)*coef+1);					
 						}
 						//Setting new rating
 						rec.setRating((r+add)*coef+1);
 					}
 				}
 			
 			this.classes = new double[classes.size()];
 			for ( j = 0; j < this.classes.length; j++) {
 				this.classes[j]=classes.get(j);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 /*
 	protected void getDefs() {
 		try {
 
 			ignoreList = Utils.getList();
 			BufferedReader in = new BufferedReader(
 					new FileReader(file + ".def"));
 			String line;
 			line = in.readLine();
 			line = in.readLine();
 			int i = 2;
 			List<Attribute> attrs = Utils.getList();
 			Attribute attr = new Attribute();
 			attr.setIndex(0);
 			attr.setName("userId");
 			attr.setType(Attribute.NUMERICAL);
 			attrs.add(attr);
 			attr = new Attribute();
 			attr.setIndex(1);
 			attr.setName("objectId");
 			attr.setType(Attribute.NUMERICAL);
 			attrs.add(attr);
 			int attrCount = 2;
 			while ((line = in.readLine()) != null) {
 				String[] attrProp = line.split(";");
 				if ("I".equals(attrProp[0])) {
 					ignoreList.add(i);
 					i++;
 					continue;
 				}
 				if(attrProp[1].equals("userId")){
 					hasUserId = true;
 					continue;
 				}
 				if(attrProp[1].equals("objectId")){
 					hasObjectId = true;
 					continue;
 				}
 				attr = new Attribute();
 				attr.setIndex(attrCount);
 				attr.setName(attrProp[1]);
 				if ("O".equals(attrProp[0])) {
 					attr.setType(Attribute.NUMERICAL);
 				} else if ("N".equals(attrProp[0])) {
 					attr.setType(Attribute.NOMINAL);
 				} else if ("R".equals(attrProp[0])) {
 					attr.setType(Attribute.NUMERICAL);
 					targetAttribute = attrCount;
 				}
 				attrs.add(attr);
 				i++;
 				attrCount++;
 			}
 			//Switch so that rating is on position 2
 			attrs.add(2,attrs.get(targetAttribute));
 			attrs.remove(targetAttribute+1);
 			in.close();
 			attributes = new Attribute[attrs.size()];
 			attrs.toArray(attributes);
 			for (int j = 0; j < attributes.length; j++) {
 				attributes[j].setIndex(j);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}*/
 
 	public Integer userId() {
 		if (this.currentUser >= userCount-1)
 			return null;
		currentUser = this.currentUser;
 		this.currentUser++;
 		return currentUser;
 	}
 	
 	public void restartUserId() {	
 		currentUser = -1;
 	}
 
 
 	/*public void setAttributes(Attribute[] attributes) {
 		this.attributes = attributes;
 		this.names = null;
 	}*/
 	public void setClasses(double[] classes) {
 		this.classes = classes;
 	}
 
 	public int getTargetAttribute() {
 		return 2;
 	}
 
 }
