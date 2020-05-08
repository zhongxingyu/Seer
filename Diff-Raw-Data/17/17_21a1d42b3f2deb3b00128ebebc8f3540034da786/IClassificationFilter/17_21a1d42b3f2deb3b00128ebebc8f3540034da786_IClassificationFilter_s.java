 package org.computer.knauss.reqtDiscussion.model;
 
 public interface IClassificationFilter {
 
 	public static final NameFilter NAME_FILTER = new NameFilter();
 
 	public DiscussionEventClassification filterCommentClassifications(
 			DiscussionEventClassification[] classifications);
 
 	public class NameFilter implements IClassificationFilter {
 
 		private String[] name = new String[] { "gpoo", "eric1" };
 
 		private NameFilter() {
 
 		}
 
 		public void setName(String name) {
 			if (name == null)
 				this.name = new String[] { "null" };
			this.name = name.split(",");
 		}
 
 		public String getName() {
 			StringBuffer b = new StringBuffer();
 			for (String s : this.name) {
 				b.append(s);
 				b.append(",");
 			}
 			return b.substring(0, b.length() - 1);
 		}
 
 		@Override
 		public DiscussionEventClassification filterCommentClassifications(
 				DiscussionEventClassification[] classifications) {
 			for (String rater : this.name) {
 				for (DiscussionEventClassification wcc : classifications)
 					if (rater.equals(wcc.getClassifiedby()))
 						return wcc;
 			}
 			return new DiscussionEventClassification();
 		}
 
 	}
 
 }
