 /*
 DMXmlJson - Java binding framework for xml and json - http://www.davide.bz/dmxj
 
 Copyright (C) 2013 Davide Montesin <d@vide.bz> - Bolzano/Bozen - Italy
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>
 */
 
 package bz.davide.dmxmljson.util;
 
 import java.util.HashMap;
 
 public class IntIntHashMap<T>
 {
    HashMap map;
 
    public IntIntHashMap()
    {
       map = new HashMap();
    }
 
    public T put(int[] keys, T obj)
    {
       HashMap tmp = map;
       for (int i = 0; i < keys.length - 1; i++)
       {
         tmp = (HashMap) tmp.get(keys[i]);
         if (tmp == null)
          {
            HashMap newmap = new HashMap();
            tmp.put(keys[i], newmap);
            tmp = newmap;
          }
       }
       return (T) tmp.put(keys[keys.length - 1], obj);
    }
 
    public void put(int k1, int k2, T obj)
    {
       this.put(new int[] { k1, k2 }, obj);
    }
 
    public void put(int k1, int k2, int k3, T obj)
    {
       this.put(new int[] { k1, k2, k3 }, obj);
    }
 
    public void put(int k1, int k2, int k3, int k4, T obj)
    {
       this.put(new int[] { k1, k2, k3, k4 }, obj);
    }
 
    public T get(int[] keys)
    {
       HashMap tmp = map;
       for (int i = 0; i < keys.length - 1; i++)
       {
          tmp = (HashMap) tmp.get(keys[i]);
          if (tmp == null)
          {
             return null;
          }
       }
       return (T) tmp.get(keys[keys.length - 1]);
    }
 
    public T get(int k1, int k2)
    {
       return this.get(new int[] { k1, k2 });
    }
 
    public T get(int k1, int k2, int k3)
    {
       return this.get(new int[] { k1, k2, k3 });
    }
 
    public T get(int k1, int k2, int k3, int k4)
    {
       return this.get(new int[] { k1, k2, k3, k4 });
    }
 }
