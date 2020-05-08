 package com.anotherbrick.inthewall;
 
 import java.util.ArrayList;
 
 import com.anotherbrick.inthewall.datasource.DataSource;
 
 public class Model {
 
   static Model instance;
   public Main p;
   public float touchX;
   public float touchY;
   public TouchEnabled currentModalVizPanel;
   public DataSource dataSource;
   public ArrayList<Object> selectedGenres = new ArrayList<Object>();
   public ArrayList<Object> selectedMonsters = new ArrayList<Object>();
   public ArrayList<Object> selectedCountries = new ArrayList<Object>();
 
   public VizList genrePanel;
   public VizList monsterPanel;
   public VizList countriesPanel;
   public VizList moviesList;
 
   public enum Datas {
     NUMBER_OF_MOVIES, AVERAGE_RATING, AVERAGE_BUDGET, AVERAGE_VOTES
   };
 
   public Datas currentDataDisplayed = Datas.NUMBER_OF_MOVIES;
 
   public static void setup(Main p) {
     Model.instance = new Model(p);
   }
 
   private Model(Main p) {
     this.p = p;
     this.dataSource = new DataSource();
   }
 
   public static Model getInstance() {
     return Model.instance;
   }
 
   public void loadFiles() {
     // TODO Auto-generated method stub
 
   }
 
 }
