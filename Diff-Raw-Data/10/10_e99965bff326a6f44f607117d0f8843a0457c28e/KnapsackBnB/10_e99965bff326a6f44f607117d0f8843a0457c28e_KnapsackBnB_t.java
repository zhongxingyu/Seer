 /**
  * Implementation of Knapsack, that uses Branch & Bound with Relaxation strategy
  */
 package main.java.knapsack;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NavigableSet;
 import java.util.TreeSet;
 
 public class KnapsackBnB implements Knapsack {
     public int size;
     public int numberOfItems;
     public List<ItemBnB> items;
     public int total = -1;
     public NavigableSet<ItemBnB> selectedItems;
 
     public KnapsackBnB(String filePath) {
 	getArray(filePath);
 	numberOfItems = items.size();
     }
 
     public KnapsackBnB() {
     }
 
     @Override
     public void getArray(String filePath) {
 	items = new ArrayList<ItemBnB>();
 	try {
 	    FileReader fr = new FileReader(filePath);
 	    @SuppressWarnings("resource")
 	    BufferedReader br = new BufferedReader(fr);
 	    String line = br.readLine();
 	    int tmp = Integer.parseInt(line.trim().split("(\\s)+")[0]);
 	    if (tmp <= 0)
 		throw new Exception("Knapstack size should be positive!");
 	    size = tmp;
 	    int name = 0;
 	    while ((line = br.readLine()) != null) {
 		String[] split = line.trim().split("(\\s)+");
 		int val = Integer.parseInt(split[0]);
 		int weig = Integer.parseInt(split[1]);
 		items.add(new ItemBnB(val, weig, ++name));
 	    }
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
 
 	// sort whole collection of items in DESC order of item's ratio
 	Collections.sort(items, new Comparator<ItemBnB>() {
 
 	    @Override
 	    public int compare(ItemBnB o1, ItemBnB o2) {
 		if (o1.ratio > o2.ratio)
 		    return -1;
 		if (o1.ratio < o2.ratio)
 		    return 1;
 		if (o1.value > o2.value)
 		    return -1;
 		return 0;
 	    }
 	});
 
     }
 
     @Override
     public int getTotalResult() {
 	if (total < 0)
 	    calculate();
 	return total;
     }
 
     @Override
     public NavigableSet<ItemBnB> getSelectedItems() {
 	if (selectedItems == null)
 	    calculate();
 	return selectedItems;
     }
 
     @Override
     public void calculate() {
 	long currMaxRes = -1;
 
 	Comparator<ItemBnB> labelComp = new Comparator<ItemBnB>() {
 	    @Override
 	    public int compare(ItemBnB o1, ItemBnB o2) {
 		if (o1.lbl > o2.lbl)
 		    return 1;
 		if (o1.lbl < o2.lbl)
 		    return -1;
 		return 0;
 	    }
 	};
 	Result result = getTempResult(0, size, new TreeSet<ItemBnB>(labelComp), 0);
 	if (result.totalValue > currMaxRes) {
 	    selectedItems = result.selectedItems;
 	    total = result.totalValue;
 	}
     }
    
     @Override
     public String getSelectedNumbers() {
 	if (selectedItems == null)
 	    calculate();
 	StringBuilder sb = new StringBuilder("Result: ");
 	Iterator<ItemBnB> li = selectedItems.iterator();
 	System.out.println(selectedItems.size());
 	while (li.hasNext()) {
 	    sb.append(li.next().lbl);
 	    if (li.hasNext())
 		sb.append(", ");
 	}
 	return sb.toString();
     }
 
     private Result getTempResult(int firstNum, long capLeft, NavigableSet<ItemBnB> selected, int currVal) {
 	if (capLeft == 0) {
 	    return new Result(currVal, selected);
 	}
 	if (firstNum >= items.size())
 	    return new Result(currVal, selected);
 
 	int currMaxRes = currVal;
 
 	NavigableSet<ItemBnB> set = null;
 
 	Result res;
 
 	ItemBnB top = items.get(firstNum);
 
 	// we can take this item or no
 	// 1 - we take it (if we can)
 	if (capLeft >= top.weight) {
 	    selected.add(top);
 
 	    res = getTempResult(firstNum + 1, capLeft - top.weight, selected, currVal + top.value);
 	    if (res.totalValue > currMaxRes) {
 		set = res.selectedItems;
 		currMaxRes = res.totalValue;
 	    }
 
 	}
 	// 2 - we don't take this item
 	double possible = getMaximumPossibleValue(firstNum + 1, capLeft);
 	if (currVal + possible > currMaxRes) {
 	    for (int j = firstNum + 1; j < items.size(); j++) {
 		if (items.get(j).weight <= capLeft) {
 		    selected.remove(top);
 		    res = getTempResult(j, capLeft, selected, currVal);
 		    if (res.totalValue > currMaxRes) {
 			set = res.selectedItems;
 			currMaxRes = res.totalValue;
 		    }
 		    break;
 		}
 	    }
 
 	}
 	return new Result(currMaxRes, set == null ? selected : set);
     }
 
     /**
      * calculates optimistic evaluation of remaining items using linear
      * relaxation
      * 
      * @param firstNum
      *            number of first item in items list to start
      * @param capLeft
      *            current capacity of knapsack
      * @return maximum possible value of items from list according to current
      *         capacity
      */
     private double getMaximumPossibleValue(int firstNum, long capLeft) {
 	double res = 0;
 	for (int i = firstNum; i < items.size(); i++) {
 	    ItemBnB item = items.get(i);
 	    if (item.weight <= capLeft) {
 		capLeft -= item.weight;
 		res += item.value;
 	    } else {
 		res += (double) capLeft * item.ratio;
 		break;
 	    }
 	}
 	return res;
 
     }
 
     // class that holds current results
     class Result {
 	int totalValue;
 	NavigableSet<ItemBnB> selectedItems;
 
 	Result(int currMaxRes, NavigableSet<ItemBnB> items) {
 	    totalValue = currMaxRes;
 	    selectedItems = items;
 
 	}
     }
 
 }
