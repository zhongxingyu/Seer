 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package heartsim.cam;
 
 import heartsim.CellularAutomaton;
 import heartsim.cam.parameter.CAModelParameter;
 import heartsim.util.StringUtils;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * Base class for cellular automata models
  * @author Lee Boynton
  */
 public abstract class CellularAutomataModel
 {
     protected int height; // height of the grid
     protected int width; // width of the grid
     protected String name; // name of the CA model
     protected String description; // description of the CA model
     protected Map<String, CAModelParameter> parameters = new TreeMap<String, CAModelParameter>();
 
     protected CellularAutomataModel(String name)
     {
         this.name = name;
     }
 
     public void setSize(int height, int width)
     {
         this.height = height;
         this.width = width;
     }
 
     /**
      * Get the name of the cellular automata model
      * @return Name
      */
     public String getName()
     {
         return name;
     }
 
     /**
      * Gets the description of the cellular automata model
      * @return
      */
     public String getDescription()
     {
         return description;
     }
 
     protected void setDescription(String description)
     {
         this.description = description;
     }
 
     /**
      * Gets a list of the parameters this cellular automata model takes
      * @return List of parameter names
      */
     public Map<String, CAModelParameter> getParameters()
     {
         return parameters;
     }
 
     public void setParameter(String name, CAModelParameter initialValue)
     {
         initialValue.setName(name);
         parameters.put(name, initialValue);
     }
 
     public CAModelParameter getParameter(String name)
     {
         return parameters.get(name);
     }
 
     /**
      * Initialises the cells
      */
     public abstract void initCells();
 
     @Override
     public String toString()
     {
         return name;
     }
 
     public abstract void printArrays();
 
     protected void printArrays(String names[], int values[][][])
     {
         for (int i = 0; i < names.length; i++)
         {
             names[i] = StringUtils.padRight(names[i], values[i].length);
         }
 
         for (int i = 0; i < names.length; i++)
         {
             for (int row = -1; row < values[i].length; row++)
             {
                 if (row == -1)
                 {
                     System.out.print(names[i] + " ");
                     continue;
                 }
 
                 for (int col = 0; col < values[i][row].length; col++)
                 {
                     System.out.print(values[i][row][col]);
                 }
 
                 System.out.print(" ");
             }
 
             System.out.println();
         }
     }
 
     /**
      * Gets the largest value which should be displayed on the chart
      * @return Max value
      */
     public abstract int getMax();
 
     /**
      * Gets the minimum value which should be displayed on the chart
      * @return Min value
      */
     public abstract int getMin();
 
     public abstract boolean isCellRecovered(int u, int v);
 
     public abstract int getCellExcitationValue();
 
     public abstract int processCell(int row, int col, int[][] u, int[][] v, int[][] tempu);
 }
