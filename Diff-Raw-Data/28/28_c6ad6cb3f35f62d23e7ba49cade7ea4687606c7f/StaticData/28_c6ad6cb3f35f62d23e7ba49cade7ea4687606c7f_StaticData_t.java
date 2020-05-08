 package com.example.l2plabv0;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 /**
  * User: arne Date: 17.11.13 Time: 15:35
  */
 public class StaticData {
 	public static List<Course> getCourseList() {
 
 		String url = "http://seoul.freehostia.com/file.json";
 		List<Course> courseAndLinks = new ArrayList<Course>();

 
 		try {
 			String jsonFromServer = new CommonHttpGETorPOST().execute(url)
 					.get();
 			JSONObject jsonObjectFromServer = new JSONObject(jsonFromServer);
 			JSONArray dataSetArray = jsonObjectFromServer
 					.getJSONArray("dataSet");
 			for (Integer c = 0; c < 3; ++c) {
                List<Link> listOfLinks = new ArrayList<Link>();
 				for (int i = 0; i < dataSetArray.length(); ++i) {
 					JSONObject singleLinkItem = dataSetArray.getJSONObject(i);
 					String singleComment = singleLinkItem.getString("comment");
 					String singleUrl = singleLinkItem.getString("Url");
 
 					listOfLinks.add(new Link(singleComment, singleUrl));
 				}
 				courseAndLinks.add(new Course("Newest L2P: "+c.toString(), listOfLinks));
 			}
 		} catch (Exception e) {
 			CommonData.writeLog("Static Data Download", e);
 		}
 		//courseAndLinks.add(new Course("Newest L2P", listOfLinks));
 
 		return courseAndLinks;
 		/*
 		 * return Arrays.asList( new
 		 * Course("languages for scientific computing", new Link("Matlab",
 		 * "http://en.wikipedia.org/wiki/MATLAB"), new Link("Mathematica",
 		 * "http://en.wikipedia.org/wiki/Mathematica"), new
 		 * Link("C programming language",
 		 * "http://en.wikipedia.org/wiki/C_%28language%29"), new
 		 * Link("numerical analysis",
 		 * "http://en.wikipedia.org/wiki/Numerical_analysis") ), new
 		 * Course("machine learning", new Link("Bayes' theorem",
 		 * "http://en.wikipedia.org/wiki/Bayes%27_theorem"), new
 		 * Link("Singular Value Decomposition",
 		 * "http://en.wikipedia.org/wiki/Singular_value_decomposition"), new
 		 * Link("Support vector machine",
 		 * "http://en.wikipedia.org/wiki/Support_vector_machine"), new
 		 * Link("k-means clustering",
 		 * "http://en.wikipedia.org/wiki/K-means_clustering"), new
 		 * Link("Regression analysis",
 		 * "http://en.wikipedia.org/wiki/Regression_analysis") ), new
 		 * Course("computer vision", new Link("image segmentation",
 		 * "http://en.wikipedia.org/wiki/Image_segmentation"), new
 		 * Link("face detection",
 		 * "http://en.wikipedia.org/wiki/Face_detection"), new
 		 * Link("pedestrian detection",
 		 * "http://en.wikipedia.org/wiki/Pedestrian_detection") ) );
 		 */
 	}
 
 	public static class Course {
 		public String name;
 		public List<Link> topics;
 
 		public Course(String name, List<Link> listOfLinks) {
 			this.name = name;
 			this.topics = listOfLinks;
 		}

        @Override
        public String toString() {
            return "Course{" +
                    "name='" + name + '\'' +
                    ", topics=" + topics +
                    '}';
        }
    }
 
 	public static class Link implements Serializable {
 		public String description;
 		public String url;
 		public String name;
 		public Boolean isBookmarked;
 		public String group; // can be empty
 		public Date lastModified;
 
 		public Link(String description, String url, String name,
 				Boolean bookmarked, String group, Date lastModified) {
 			this.description = description;
 			this.url = url;
 			this.name = name;
 			isBookmarked = bookmarked;
 			this.group = group;
 			this.lastModified = lastModified;
 		}
 
 		public Link(String name, String url) {
 			this.description = url;
 			this.url = url;
 			this.name = name;
 			isBookmarked = false;
 			this.group = "";
 			this.lastModified = new Date();
 		}

        @Override
        public String toString() {
            return "Link{" +
                    "description='" + description + '\'' +
                    ", url='" + url + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
 }
