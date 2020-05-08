 /* A Specimen is a knapsack packed with an ordered list of objects using a first-fit algorithm.
  * 
  */
 
 import java.util.ArrayList;
 import java.util.Random;
 
 public class Specimen{
 	
 	private int [][] knapsack;
 	private int numItems;
 	private ArrayList<KnapsackObject> objectArray;	
 	private int fitness;
 	
 	/* Constructor. Creates a Specimen with a given knapsack size, number of objects, list of objects, and whether or not the object order is random */
 	public Specimen(int knapsackWidth, int knapsackLength, int numItems, ArrayList<KnapsackObject> objectArray, boolean random)
 	{
 		knapsack = new int[knapsackWidth][knapsackLength];
 		this.numItems = numItems;
 		
 		//The object order isn't random. Keep the list as-is.
 		if(!random)
 		{
 			this.objectArray = objectArray;		
 		}
 		//The object order is random. Randomize both the order of the list and the orientation of objects.
 		else	//Randomly sets up objectArray;
 		{
 			Random rGen;
 			boolean swapSides;
 			int currentPosition;
 			ArrayList<Integer> usedArrayPositions;
 			KnapsackObject [] tempArray;
 			KnapsackObject tempObject;
 			
 			rGen = new Random();
 			swapSides = false;
 			usedArrayPositions = new ArrayList<Integer>();
 			tempArray = new KnapsackObject[objectArray.size()];
 			this.objectArray = new ArrayList<KnapsackObject>();
 			
 			//Randomly orders objects
 			for(int i = 0; i < numItems; i++)
 			{
 				swapSides = rGen.nextBoolean();
 				
 				do
 				{
 					currentPosition = rGen.nextInt(numItems);
 					
 				} while(usedArrayPositions.contains(currentPosition));
 				
 				usedArrayPositions.add(currentPosition);
 				
 				tempObject = objectArray.get(i);
 				
 				//Randomly orients objects
 				if(swapSides)		
 					tempObject.swapSides();
 				
 				tempArray[currentPosition] = tempObject;
 			}
 			
 			//Initializes objectArray with this new random ordering
 			for(int i = 0; i < numItems; i++)
 				this.objectArray.add(tempArray[i]);
 		}
 		
 		//Updates fitness (packs the knapsack)
 		fitness = 0;
 		determineFitness();
 	}
 	public void print()
 	{
 		System.out.println();
 		for(int j = 0; j < knapsack[0].length; j++)
 		{
 			for(int i = 0; i < knapsack.length; i++)
 			{
 				System.out.print(knapsack[i][j]+ " ");
 			}
 			System.out.println();
 		}
 	}
 
 	
 	/* Updates the knapsack with an object and it's starting position. Should only be called from tryToFit(), which should only be called from pack() */
 	private void placeObject(int startX, int startY, int width, int length, int positionInArray)
 	{
 		positionInArray++;	//Passed the actual index, 0-based. Converts to 1-based.
 		
 		for(int j = 0; j < length; j++)
 		{
 			for(int i = 0; i < width; i++)
 			{
 				//System.out.println("Writing " + positionInArray + " to " + i + " " + j);
 				knapsack[startX + i][startY + j] = positionInArray;				
 			}
 		}
 		
 		
 	}
 	
 	/* Fairly naive approach. Attemps to fit some object in the knapsack in the upper-left-most corner. 
 	 * 		Moves right and then wraps around (x = 0, y++) looking for possible spots to place the item.
 	 */
 	private boolean tryToFit(KnapsackObject object, int indexInArray)
 	{
 		boolean canFit;
 		
 		canFit = true;
 		
 		for(int j = 0; j < knapsack[0].length; j++)
 		{
 			for(int i = 0; i < knapsack.length; i++)
 			{
 				for(int m = 0; m < object.getLength(); m++)
 				{
 					for(int n = 0; n < object.getWidth(); n++)
 					{
 						if( (i + n >= knapsack.length) || (j + m >= knapsack[0].length) || (knapsack[i+ n][j + m] != 0))	//If the item won't fit in the xDirection, yDirection, or a space "in the item" is already occupied
 							canFit = false;	//The item can't fit!
 						
 						//End-of-object check for fit in knapsack
 						if(m + 1 == object.getLength() && n + 1 == object.getWidth() && canFit)	//It fits!
 						{
 							placeObject(i, j, object.getWidth(), object.getLength(), indexInArray);	//Place the object
 							return canFit;	//Return successful!
 						}
 						else if(m + 1 == object.getLength() && n + 1 == object.getWidth() && !canFit)	//It doesn't fit. Increment starting position and try again.
 							canFit = true;
 						
 					}
 					
 				}
 				
 			}
 		}
 		
 		//If the code reaches here, the item wasn't able to be placed.
 		canFit = false;	//Not really needed, but the loop exits with canFit = true. Can just return false instead
 		
 		return canFit;
 	}
 	
 	/* Attempts to pack all objects into the knapsack */
 	public void pack()
 	{
 		int indexOfObject;
 		int numItemsFit;
 		
 		indexOfObject = 0;
 		numItemsFit = numItems;
 
 		
 		for(KnapsackObject object : objectArray)
 		{
 			if(!tryToFit(object, indexOfObject))	//If the item can't be fit using the algorithm, update the numItemsFit
 				numItemsFit--;
 			indexOfObject++;	//Combination for & for each. Probably bad practice.
 		}
 		
 		//If all items were able to fit
 		if(numItemsFit == numItems)
 			calculateGoodFitness();
 		else
 			calculateBadFitness(numItemsFit);
 
 			
 	}
 	
 	
 	/* Creates an arbitrary *very high* number in relation to Specimen's that managed to fit all objects into the grid. */
 	public void calculateBadFitness(int numItemsFit)
 	{
 		//The more items fit, the "more fit" it is, regardless of object size. This is quick and dirty, can be improved
		fitness = knapsack.length * knapsack[0].length * numItemsFit * 1000;
 	}
 	
 	/* Finds and sets fitness to minimum bounding box for current assignment, only if all objects are placed */
 	private void calculateGoodFitness()
 	{
 		int width;
 		int length;
 		int highestWidth;
 		int highestLength;
 		
 		highestWidth = 0;
 		highestLength = 0;
 		width = 0;
 		length = 0;
 		for(int i = 0; i < knapsack.length; i++)
 		{
 			for(int j = 0; j < knapsack[0].length; j++)
 			{
 				if(knapsack[i][j] != 0)
 					length = j + 1;
 			}
 			if(length > highestLength)
 				highestLength = length;
 			length = 0;
 		}
 		
 		for(int j = 0; j < knapsack[0].length; j++)
 		{
 			for(int i = 0; i < knapsack.length; i++)
 			{
 				if(knapsack[i][j] != 0)
 					width = i + 1;
 			}
 			if(width > highestWidth)
 				highestWidth = width;
 			width = 0;
 		}
 		
 		fitness = highestWidth * highestLength;
 	}
 
 	//Wrapper for pack()
 	public void determineFitness()
 	{
 		pack();
 	}
 	
 	/* Accessors and mutators for private variables */
 	public int [][] getKnapsack()
 	{
 		return knapsack;
 	}
 	
 	public ArrayList<KnapsackObject> getObjectArray()
 	{
 		return objectArray;
 	}
 	
 	public void setObjectArray(ArrayList<KnapsackObject> objectArray)
 	{
 		this.objectArray = objectArray;
 	}
 
 	public int getNumItems()
 	{
 		return numItems;
 	}
 
 	public int getFitness()
 	{
 		return fitness;
 	}
 
 }
