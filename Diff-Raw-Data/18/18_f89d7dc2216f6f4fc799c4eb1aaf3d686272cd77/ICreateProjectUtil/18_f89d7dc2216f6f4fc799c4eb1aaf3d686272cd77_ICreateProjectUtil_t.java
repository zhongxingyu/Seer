 package org.jabox.application;
 
 import org.jabox.model.Project;
 
import com.google.inject.ImplementedBy;

@ImplementedBy(CreateProjectUtil.class)
 public interface ICreateProjectUtil {
 
 	public abstract void createProject(final Project project);
 
 }
