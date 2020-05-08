 package org.devilry.core.entity;
 
 import javax.persistence.*;
 
 @Entity
 @DiscriminatorValue("AN")
 public class AssignmentNode extends Node {
 	public AssignmentNode() {
 
 	}
 }
