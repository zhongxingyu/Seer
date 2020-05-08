 package pubCrawl.core;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class PCObjectTest {
 
     @Test
     public void objectCreation() {
         /*
             mynum = 5;
             mystring = "hey";
             myobject = {
                 'a': mynum,
                 'b': 'testing',
                 'c': (x,y) -> x + y,
                 'd': {
                     'x': 5',
                     'y': 'hey'
                 }
             };
          */
 
         PCObject mynum = new PCObject(5);
         //PCObject mystring = "hey"; TODO handle strings
 
         PCObject myObject = new PCObject();
         myObject.set("a", mynum);
         myObject.set("b", "testing");
         class func1 {
             public PCObject call(PCObject x, PCObject y) {
                 return new PCObject(x.<Double>getBase() + y.<Double>getBase());
             }
         }
         myObject.set("c", new func1());
 
         PCObject __d = new PCObject();
         __d.set("x", 5);
         __d.set("y", "hey");
         myObject.set("d", __d);
 
         //calling c....
         //myObject.c(4,5); //9
         //TODO oh god this is ambiguous because x.add(y) could be add(x,y) or x has a property function add....
         func1 func1instance1 = myObject.get("c"); //how do we know it's func1? We mapped c to func1...fun
         PCObject ans = func1instance1.call(new PCObject(4), new PCObject(5));
         System.out.println(ans.<Double>getBase());
     }
 
     @Test
     public void bubbleSort(){
     /*
 1         bubble(values) -> {
 2             swapped = true;
 3             while(swapped) {
 4                 swapped = false;
 5                 for(i = 0; i < values.length-1; i++) {
 6                     if (values[i] > values[i+1]) {
 7                         temp = values[i];
 8                         values[i] = values[i+1];
 9                         values[i+1] = temp;
 10                        swapped = true;
 11                    }
 12                }
 13            }
 14            return values;
 15        }
      */
 
         // HERE IS WHAT THE COMPILER WOULD THINK
         //at line 5, we know that values must be a PCObject with a property 'length'
         //at line 5, we know that i is an integer
         //at line 6 we know that values must be a PCList (:PCObject) because of index notation WITH INTEGER
         //Therefore we know bubble must take a pclist!
 
         PCList values = new PCList(); //this was passed in with some values
         values.add(new PCObject(5)); //so for now i'll just put em in here
         values.add(new PCObject(8));
         values.add(new PCObject(2));
         values.add(new PCObject(4));
         values.add(new PCObject(7));
         values.add(new PCObject(3));
 
         //at this point we're writing the code that would be written to represent the function
         PCObject swapped = new PCObject(true);
         while (swapped.<Boolean>getBase()) { //we know that a while loop needs a boolean, so swapped must be a boolean
             swapped = new PCObject(false);
             for(int i = 0; i < values.size()-1; i++){ //if i was already declared, ideally we can use it, but "int" goes away
                 if(values.get(i).<Double>getBase() > values.get(i + 1).<Double>getBase()) { // > only with nums yo
                     PCObject temp = values.get(i);
                     values.set(i, values.get(i + 1));
                     values.set(i + 1, temp);
                     swapped = new PCObject(true);
                 }
             }
         }
         //FOR OUR PURPOSES, LET'S PRINT THIS OUT TO MAKE SURE IT WORKS
         for(int j = 0; j < values.size(); j++){
             System.out.println(values.get(j).<Double>getBase());
         }
     }
 
     @Test
     public void bubbleSortAlden() {
 
         // HERE IS THE PUBCRAWL CODE:
         /*
             1  bubble(values) -> {
             2      swapped = true;
             3      while(swapped) {
             4          swapped = false;
             5          for(i = 0; i < values.length-1; i++) {
             6              if (values[i] > values[i+1]) {
             7                  temp = values[i];
             8                  values[i] = values[i+1];
             9                  values[i+1] = temp;
             10                 swapped = true;
             11             }
             12         }
             13     }
             14     return values;
             15 }
          */
 
         // AND THE JAVA CODE:
         IPCFunction bubble = new IPCFunction() {
             @Override
             public PCObject call(PCObject... args) {
 
                 // "bubble" takes 1 argument "values", which we know is a PCList because:
                 // from line 5: we know that i is an integer
                 // from line 6: we know that values must be a PCList because integer indexing is only valid on lists
                 PCList values = (PCList)args[0];
 
                 // make a copy of the list because function arguments are passed by "value"
                 values = values.subList(0, values.size());
 
                 // "swapped" is just a boolean
                 PCObject swapped = new PCObject(true);
 
                 while (swapped.<Boolean>getBase()) {
 
                     // any time a variable is set, it becomes a new PCObject, because underlying type could change
                     swapped = new PCObject(false);
 
                     for (int i = 0; i < values.size() - 1; i++) {
 
                         // from line 6: we know that "values" must be a list of doubles
                         // because ">" operator is only valid on numbers
                         if (values.get(i).<Double>getBase() > values.get(i + 1).<Double>getBase()) {
 
                             // now performing the swap is a straightforward translation
                             PCObject temp = values.get(i);
                             values.set(i, values.get(i + 1));
                             values.set(i + 1, temp);
                             swapped = new PCObject(true);
                         }
                     }
                 }
 
                 // done!
                 return values;
             }
         };
 
         // NOW THE TEST
 
         // initial list is [5,8,2,7,3,2]
         PCList values = new PCList();
         values.add(new PCObject(5));
         values.add(new PCObject(8));
         values.add(new PCObject(2));
         values.add(new PCObject(7));
         values.add(new PCObject(3));
         values.add(new PCObject(2));
 
         // call the method
         PCObject resultObj = bubble.call(values);
         PCList result = (PCList)resultObj;
 
         // new list should be [2,2,3,5,7,8]
         Assert.assertEquals(2.0, result.get(0).getBase());
         Assert.assertEquals(2.0, result.get(1).getBase());
         Assert.assertEquals(3.0, result.get(2).getBase());
         Assert.assertEquals(5.0, result.get(3).getBase());
         Assert.assertEquals(7.0, result.get(4).getBase());
         Assert.assertEquals(8.0, result.get(5).getBase());
     }

 }
