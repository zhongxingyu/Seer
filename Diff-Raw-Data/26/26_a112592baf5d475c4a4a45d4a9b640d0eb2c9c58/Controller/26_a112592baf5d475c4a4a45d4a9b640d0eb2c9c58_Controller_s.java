 package controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.*;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.EnumSet;
 import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
 import model.Model;
 import model.Movie;
 import views.IUpdateView;
 
 /**
  *
  * @author Cody W. Eilar <Cody.Eilar@Gmail.com>
  */
 public class Controller implements ActionListener {
 
     Model model;
     CopyOnWriteArrayList<IUpdateView> viewList;
    String[] dirs = {"movies", "videos"};
 
     public Controller(Model m) {
 	this.model = m;
 	viewList = new CopyOnWriteArrayList();
 
     }
 
     public void addViews(IUpdateView iv) {
 	viewList.add(iv);
     }
 
     public void removeViews(IUpdateView iv) {
 	viewList.remove(iv);
     }
 
     public void populateMovies() throws IOException {
 	//Get search paths for movies 
 	model.getMovies().removeAllMovies();
 	MoviePathSearcher mps = new MoviePathSearcher(model);
 	File[] roots = File.listRoots();
 	for (File r : roots) {
 	    Path p = Paths.get(r.getAbsolutePath());
 	    EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
 	    Files.walkFileTree(p, opts, model.getSearchDepth(), mps);
 	}
 
 	MovieSearcher ms = new MovieSearcher(model);
 	//Get all the paths that supposedly have movies
 	Path[] paths = mps.getPathsToMovies();
 
 	//search for all the movies within those directories... no limit to depth
 	for (Path path : paths) {
 	    if (path != null) {
 		Files.walkFileTree(path, ms);
 	    }
 	}
 
	//upate all teh vewis with the new model data. 
 	updateViews();
 
 	for (Movie mov : model.getMovies().getMovieList()) {
 	    System.out.println("movies in list:" + mov.getTitle());
 	}
     }
 
     public void updateViews() {
 	for (IUpdateView iv : viewList) {
 	    if (iv != null) {
 		iv.updateView();
 	    }
 	}
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
 	String cmd = e.getActionCommand();
 
 	if (cmd.equals("Add Keyword")) {
 	}
 
     }
 }
