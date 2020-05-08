 public class DSolve {
   
 	public void dSolve(Puzzle solving){
 	    int row = 0;
 	    int column = 0;
 	    int value =0;
 	    boolean done= false;
 	 
 	    
 	    while(done != false){
 	    	    
 	     while(column!=9){  
 	    	  while(row!=9){
 	    		if(solving.getValueAtPosition(column,row) != 0 && solving.getPosition(column,row).getListSize() !=0){
 	    			value = solving.getValueAtPosition(column,row);
 	    			setImpossible(solving,column,row,value);
 	    		}
 	    		setValueSingleElement(solving);
 	    		row++;
 	    	  }  
 	    	row = 0;
 	    	column++;
 	    }
 	     row = 0;
 	     column = 0;
 	     done = filled(solving);
 	   }
 	    
 	    
 	    	
 	}
 	
 	public boolean filled(Puzzle solving){
 	    int column = 0;
 	    int row = 0;
		boolean filled = true;
 	    
 	 
 	    	while(column!=9 && filled == true){  //Checks if we have put something in everysquare
 	    	  while(row!=9){
 	    		if(solving.getValueAtPosition(column,row) == 0){
 	    			filled = false;
 	    		}
 	    		row++;
 	    	  }
 	    	row = 0;
 	    	column++;
 	       }
 	    
         return filled;
 	}
 	
 	
 	public void setImpossible(Puzzle a,int row, int column, int value){
 	    int rowTmp = 0;
 	    int colTmp = 0;
 	    
 	    while(colTmp!=9){
 	    	a.getPosition(colTmp,row).removeVal(value);
 	    	colTmp++;
 	    }
 	    
 	    while(rowTmp!=9){
 	    	if(a.getPosition(column, rowTmp).checkContains(value)){
 	    	a.getPosition(column,rowTmp).removeVal(value);
 	    	}
 	    	rowTmp++;
 	    }
 	    
 	    //Square 1
 	    rowTmp = 0;
 	    colTmp = 0;
 	    if(row < 3 && column < 3){
 	    	while(rowTmp != 3){
 	    		while(colTmp!=3){
 	    			if(a.getPosition(column, rowTmp).checkContains(value)){
 	    		    	a.getPosition(column,rowTmp).removeVal(value);
 	    		    }
 	    			colTmp++;
 	    		}
 	    		   colTmp = 0;
 	    		   rowTmp++;
 	    	}
 	    }
 	    
 	    //Square 2
 	    rowTmp = 0;
 	    colTmp = 3;
 	    if(row < 3 && column < 6 && column > 2){
 	    	while(rowTmp != 3){
 	    		while(colTmp!=6){
 	    			if(a.getPosition(colTmp, rowTmp).checkContains(value)){
 	    		    	a.getPosition(colTmp,rowTmp).removeVal(value);
 	    		    }
 	    			colTmp++;
 	    		}
	    		   colTmp = 3;
 	    		   rowTmp++;
 	    	}
 	    }
 	    
 	    //Square 3
 	    rowTmp = 0;
 	    colTmp = 6;
 	    if(row < 3 && column < 9 && column > 5){
 	    	while(rowTmp != 3){
 	    		while(colTmp!=9){
 	    			if(a.getPosition(colTmp, rowTmp).checkContains(value)){
 	    		    	a.getPosition(colTmp,rowTmp).removeVal(value);
 	    		    }
 	    			colTmp++;
 	    		}
 	    		   colTmp = 6;
 	    		   rowTmp++;
 	    	}
 	    }
 	    
 	    //Square 4
 	    rowTmp = 3;
 	    colTmp = 0;
 	    if(row < 6 && column < 3 && row > 2){
 	    	while(rowTmp != 6){
 	    		while(colTmp!=3){
 	    			if(a.getPosition(colTmp, rowTmp).checkContains(value)){
 	    		    	a.getPosition(colTmp,rowTmp).removeVal(value);
 	    		    }
 	    			colTmp++;
 	    		}
 	    		   colTmp = 0;
 	    		   rowTmp++;
 	    	}
 	    }
 	    
 	    //Square 5
 	    rowTmp = 3;
 	    colTmp = 3;
 	    if(row < 6 && column < 6 && row > 2 && column > 2){
 	    	while(rowTmp != 6){
	    		while(colTmp!=6){
 	    			if(a.getPosition(colTmp, rowTmp).checkContains(value)){
 	    		    	a.getPosition(colTmp,rowTmp).removeVal(value);
 	    		    }
 	    			colTmp++;
 	    		}
 	    		   colTmp = 3;
 	    		   rowTmp++;
 	    	}
 	    }
 	    
 	    //Square 6
 	    rowTmp = 3;
 	    colTmp = 6;
 	    if(row < 6 && column < 9 && row > 2 && column < 5){
	    	while(rowTmp != 6){
	    		while(colTmp!=9){
 	    			if(a.getPosition(colTmp, rowTmp).checkContains(value)){
 	    		    	a.getPosition(colTmp,rowTmp).removeVal(value);
 	    		    }
 	    			colTmp++;
 	    		}
 	    		   colTmp = 6;
 	    		   rowTmp++;
 	    	}
 	    }
 	    
 	    //Square 7
 	    rowTmp = 6;
 	    colTmp = 0;
 	    if(row < 9 && column < 3 && row > 5){
 	    	while(rowTmp != 9){
	    		while(colTmp!=3){
 	    			if(a.getPosition(colTmp, rowTmp).checkContains(value)){
 	    		    	a.getPosition(colTmp,rowTmp).removeVal(value);
 	    		    }
 	    			colTmp++;
 	    		}
 	    		   colTmp = 0;
 	    		   rowTmp++;
 	    	}
 	    }
 	    
 	    //Square 8
 	    rowTmp = 6;
 	    colTmp = 3;
 	    if(row < 9 && column < 6 && column > 2 && row > 5){
	    	while(rowTmp != 9){
	    		while(colTmp!=6){
 	    			if(a.getPosition(colTmp, rowTmp).checkContains(value)){
 	    		    	a.getPosition(colTmp,rowTmp).removeVal(value);
 	    		    }
 	    			colTmp++;
 	    		}
	    		   colTmp = 3;
 	    		   rowTmp++;
 	    	}
 	    }
 	    
 	    //Square 9
 	    rowTmp = 6;
 	    colTmp = 6;
 	    if(row < 9 && column < 9 && row > 5 && column > 5){
 	    	while(rowTmp != 9){
 	    		while(colTmp!=9){
 	    			if(a.getPosition(colTmp, rowTmp).checkContains(value)){
 	    		    	a.getPosition(colTmp,rowTmp).removeVal(value);
 	    		    }
 	    			colTmp++;
 	    		}
 	    		   colTmp = 0;
 	    		   rowTmp++;
 	    	}
 	    }
 	}
 	
 	public void setValueSingleElement(Puzzle solving){
 		int value = 0;
 		int row = 0;
 		int column = 0;
 	
 		while(row != 9){
 			while(column !=9){
 				if(solving.getPosition(column, row).getListSize() == 1 && solving.getValueAtPosition(column,row) == 0){
 					value = solving.getPosition(column, row).getLastVal();
 					solving.changeValueAtPosition(column, row, value);
 				}
 				column++;
 			}
 			column = 0;
 			row++;
 		}
 	}
 
 }
 	
 	
 
 
