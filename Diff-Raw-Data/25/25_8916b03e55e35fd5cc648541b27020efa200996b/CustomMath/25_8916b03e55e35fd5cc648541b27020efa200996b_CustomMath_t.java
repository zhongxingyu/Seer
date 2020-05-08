 /**
    Author: Travis Knight
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 public class CustomMath {
    
   /**
      Calculates the factorial for a given positive integer
      @param num The integer to calculate the factorial for
      @return The factorial for the given integer, OR
              -1 if an error occurs.
   */
    public static int factorial(int num) {
      try {
         // return error when number is negative
         if(num < 0) {
            return -1;
         }
         
         int result = 1;
         for(int i = 0; i < num; num--) {
            result *= num;
         }
         return result; // return factorial for num
      } catch (Exception e) {
         return -1; // error occured
       }
    }
 
 }
