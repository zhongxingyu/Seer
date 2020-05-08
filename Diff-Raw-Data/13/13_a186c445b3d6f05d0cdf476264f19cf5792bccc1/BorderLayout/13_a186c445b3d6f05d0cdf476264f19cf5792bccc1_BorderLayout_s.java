 package org.vaadin.smartgwt.server.layout;
 
 import static org.vaadin.smartgwt.server.builder.HLayoutBuilder.*;
 
 import org.vaadin.smartgwt.server.Canvas;
 import org.vaadin.smartgwt.server.types.Alignment;
 
 public class BorderLayout extends VLayout {
 	private HLayout north;
 	private HLayout west;
 	private HLayout center;
 	private HLayout east;
 	private HLayout south;
 
 	public BorderLayout() {
 		//@formatter:off
 		setMembers(
 			north = buildHLayout()
 				.setHeight(0)
 				.setWidth("100%")
 				.build(),
 			buildHLayout()
 				.setHeight("*")
 				.setMembers(
 					west = buildHLayout()
 						.setWidth(0)
 						.setHeight("100%")
 						.build(),
 					buildHLayout()
 						.setWidth("*")
 						.setAlign(Alignment.RIGHT)
 						.setMembers(
 							center = buildHLayout()
 								.setWidth(0)
								.setHeight(0)
 								.build(),
 							east = buildHLayout()
 								.setWidth(0)
 								.build()
 						)
 						.build()
 				)
 				.build(),
 			south = buildHLayout()
 				.setHeight(0)
 				.setWidth("100%")
 				.build()
 		);
 		//@formatter:on
 	}
 
 	public Canvas getCenterMember() {
 		return center.getMembers().length > 0 ? center.getMembers()[0] : null;
 	}
 
 	public void setCenterMember(Canvas member) {
 		if (member == null) {
 			center.setWidth(0);
 			center.setHeight(0);
 			center.setMembers();
 		} else {
 			member.setHeight("100%");
 			member.setWidth("100%");
			center.setHeight(1);
			center.setWidth(1);
 			center.setMembers(member);
 		}
 	}
 
 	public Canvas getNorthMember() {
 		return north.getMembers().length > 0 ? north.getMembers()[0] : null;
 	}
 
 	public void setNorthMember(Canvas member) {
 		if (member == null) {
 			north.setHeight(0);
 			north.setMembers();
 		} else {
 			member.setWidth("100%");
 			north.setHeight(1);
 			north.setMembers(member);
 		}
 	}
 
 	public Canvas getSouthMember() {
 		return south.getMembers().length > 0 ? south.getMembers()[0] : null;
 	}
 
 	public void setSouthMember(Canvas member) {
 		if (member == null) {
 			south.setHeight(0);
 			south.setMembers();
 		} else {
 			member.setWidth("100%");
 			south.setHeight(1);
 			south.setMembers(member);
 		}
 	}
 
 	public Canvas getWestMember() {
 		return west.getMembers().length > 0 ? west.getMembers()[0] : null;
 	}
 
 	public void setWestMember(Canvas member) {
 		if (member == null) {
 			west.setWidth(0);
 			west.setMembers();
 		} else {
 			member.setHeight("100%");
 			west.setWidth(1);
 			west.setMembers(member);
 		}
 	}
 
 	public Canvas getEastMember() {
 		return east.getMembers().length > 0 ? east.getMembers()[0] : null;
 	}
 
 	public void setEastMember(Canvas member) {
 		if (member == null) {
 			east.setWidth(0);
 			east.setMembers();
 		} else {
 			member.setHeight("100%");
 			east.setWidth(1);
 			east.setMembers(member);
 		}
 	}
 }
