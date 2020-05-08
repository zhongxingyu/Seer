 package com.media2359.euphoria.view.client.project;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.editor.client.Editor.Path;
 import com.google.gwt.user.client.ui.Composite;
 import com.media2359.euphoria.view.message.project.Project;
 import com.sencha.gxt.core.client.ValueProvider;
 import com.sencha.gxt.data.shared.ListStore;
 import com.sencha.gxt.data.shared.ModelKeyProvider;
 import com.sencha.gxt.data.shared.PropertyAccess;
 import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
 import com.sencha.gxt.widget.core.client.grid.ColumnModel;
 import com.sencha.gxt.widget.core.client.grid.Grid;
 import com.sencha.gxt.widget.core.client.grid.GridView;
 
 public class BasicGrid extends Composite {
 	GridView<Project> gridView;
 	Grid<Project> grid;
 	ListStore<Project> listStore;
 
 	// Property access definitions for the values in the Project object
 	public interface GridProperties extends PropertyAccess<Project> {
 		@Path("id")
 		ModelKeyProvider<Project> key();
 
 		ValueProvider<Project, String> id();
 		
 		ValueProvider<Project, String> name();
 
 		ValueProvider<Project, String> description();
 		
 		ValueProvider<Project, String> projectManager();
 	}
 
 	// Setup the property access definitions for the values for the grid columns
 	public static GridProperties gridProperties = GWT
 			.create(GridProperties.class);
 
 	public BasicGrid() {
 		// Setup the ListStore.
 		listStore = new ListStore<Project>(gridProperties.key());
 
 		// Setup the grid columns
 		ColumnConfig<Project, String> idCol = new ColumnConfig<Project, String>(
 				gridProperties.id(), 100, "Project Id");
 		ColumnConfig<Project, String> nameCol = new ColumnConfig<Project, String>(
 				gridProperties.name(), 150, "Project Name");
 		ColumnConfig<Project, String> descCol = new ColumnConfig<Project, String>(
 				gridProperties.description(), 150, "Project Description");
 		ColumnConfig<Project, String> projMng = new ColumnConfig<Project, String>(
 				gridProperties.projectManager(), 150, "Project Manager");
 
 		List<ColumnConfig<Project, ?>> columns = new ArrayList<ColumnConfig<Project, ?>>();
 		columns.add(idCol);
 		columns.add(nameCol);
 		columns.add(descCol);
 		columns.add(projMng);
 		ColumnModel<Project> columnModel = new ColumnModel<Project>(columns);
 
 		// Setup the grid view for styling
 		gridView = new GridView<Project>();
 		gridView.setAutoExpandColumn(nameCol);
 		gridView.setAutoExpandColumn(descCol);
 
 		// Setup the grid
 		grid = new Grid<Project>(listStore, columnModel, gridView);
 		initWidget(grid);
 	}
 
 	public void populateData(List<Project> projects) {
 		listStore.replaceAll(projects);
 	}
 	
 	public void clear() {
 		listStore.clear();
 	}
 }
