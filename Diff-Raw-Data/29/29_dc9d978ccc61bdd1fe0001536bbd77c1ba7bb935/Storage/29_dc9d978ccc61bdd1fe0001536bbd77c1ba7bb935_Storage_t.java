 /**
  
  **/
  //package GameLogic;
  
  import java.util.*;
  
  class Pair<K,V>
  {
    private K key;
    private V value;
 
    public Pair(K key, V value)
    {
      this.key = key;
 	 this.value = value;
    }
 
    public K getKey()
    {
      return key;
    }
    
    public V getValue()
    {
      return value;
    }
    
  }
  
  public class Storage
  {   
    private int [][] resourceMap;
    private int [][] distMap;
    private int [][] degMap;
    private ArrayList<City> cityDic;
    private ArrayList<Resource> resourceDic;
    private int cityCur;
    private int resourceCur;     
    
    public Storage(ArrayList<City> clist, ArrayList<Resource> rlist)
    {
      cityCur = 0;
 	 resourceCur = 0;
 	 
      resourceMap = new int[rlist.size()][clist.size()];
 	 distMap = new int [clist.size()][clist.size()];
 	 degMap = new int [clist.size()][clist.size()];
 	 
 	 cityDic = clist;
 	 resourceDic = rlist;
 	 
      for(int i=0;i<rlist.size();i++)
 	 {
 	   for(int j=0;j<clist.size();j++)
 	   {
 	     resourceMap[i][j] = -1;
 	   }
 	 }	 
 	 // assign each city its own available starting resource
 	 for(int i=0;i<clist.size();i++)
 	 {	   
 	   resourceMap[getResource(cityDic.get(i).resources.get(0))][getCity(cityDic.get(i))] = 0;
 	 }
 	 
    }
    
    public void setResourceMap()
    {
     // list of source city
 	 LinkedList<Integer> list = new LinkedList<Integer>();
 	 
 	 for(int i=0;i<resourceMap.length;i++)
 	 {
	   // get all source city, add them to list
 	   for(int j=0;j<resourceMap[i].length;j++)
 	   {
 	     if(resourceMap[i][j]==0)
 		 {
 		   list.add(j);
 		 }
 	   }
       
	   // loop through all cities again
 	   for(int h=0;h<resourceMap[i].length;h++)
 	   {
 	     for(int k=0;k<list.size();k++)
	     {
           // source city		 
 		   int t = list.get(k);
 	       int dist = degMap[t][h];
 		   if(resourceMap[i][h] < 0)
 		   {
 		     resourceMap[i][h] = dist;
 		   }
		   else if(dist < resourceMap[i][h] & dist != 0)
 		   {
 		     resourceMap[i][h] = dist;
 		   }
	     }		 
 	   }
	 // clear source list
     list.clear();
 	 }
 	 
    }
    
    public int getDegMap(int i, int j)
    {
      return degMap[i][j];
    }
    
    // **implement degree traversal algorithm here to obtain all available resources to all city 
    public void setCityDegree()
    {
      // Need to implement breadth-first search here
      for(int i=0;i<degMap.length;i++)
 	 {
 	   for(int j=0;j<degMap[i].length;j++)
 	   {
 	     degMap[i][j] = getDeg(i,j);
 	     degMap[j][i] = degMap[i][j];
 	   }
 	 }	 
    }
    
    // private helper method
    private int getDeg(int i, int j)
    {     
      LinkedList<Integer> queue = new LinkedList<Integer>();
 	 // <City, degree>
 	 Hashtable<Integer, Integer> set = new Hashtable<Integer, Integer>();
 	 int deg = 0;
 	 int cur = i;
 	 
 	 queue.add(i);
 	 set.put(i,deg);
 	 while(queue.size() > 0)
 	 {
 	   for(int k=0;k<distMap.length;k++)
 	   {
 	     if(distMap[cur][k]!=0)
 	     {
 		   queue.add(k);
 		   set.put(k,set.get(cur)+1);
 		 }
 	   }
 	   if(queue.contains(j))
 	   {
 	     queue.clear();
 	   }
 	   else
 	   {
 	     cur = queue.poll();
 	   }
 	 }
 	 
 	 return set.get(j);
    }
    
    public void setCityDist(City city1, City city2, int dist)
    {
      distMap[getCity(city1)][getCity(city2)] = dist;
 	 distMap[getCity(city2)][getCity(city1)] = dist;
 	 // set degree
 	 degMap[getCity(city1)][getCity(city2)] = 1;
 	 degMap[getCity(city2)][getCity(city1)] = 1;
    }
    
    public int getCityDist(City city1, City city2)
    {
      return distMap[getCity(city1)][getCity(city2)];
    }
    
    public int getCityResource(City city, Resource resource)
    {
 	 return resourceMap[getResource(resource)][getCity(city)];
    }
    
    private int getCity(City city)
    {
      Integer v = cityDic.indexOf(city);
 
 	 return (int) v; 	 
    }
    
    private int getResource(Resource resource)
    {
      Integer v = resourceDic.indexOf(resource);
 
 	 return (int) v; 	 
    }
    
 
    
    
    
 
  }
