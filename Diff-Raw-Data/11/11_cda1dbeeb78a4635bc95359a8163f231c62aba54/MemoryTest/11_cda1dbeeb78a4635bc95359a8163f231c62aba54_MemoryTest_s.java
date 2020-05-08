 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.model;
 
 import se.chalmers.dat255.sleepfighter.challenge.memory.Memory;
 import se.chalmers.dat255.sleepfighter.utils.debug.Debug;
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 public class MemoryTest extends TestCase {
 
 	// there should be an exception if the number of cards is odd. 
 	public void testOdd()  {
 		try {
 			new Memory(3,3);
 			Assert.fail("Should have thrown IllegalArgumentException");
 			
 		} catch (IllegalArgumentException e) {
 			// success
 		}
 		
 		Memory mem = new Memory(4,3);
 		assertEquals(4, mem.getRows());
 		assertEquals(3, mem.getCols());
 		assertEquals(12, mem.getNumCards());
 		assertEquals(6, mem.getNumPairs());
 	}
 	
 	public boolean testPlaceOutCards(int rows, int cols) {
 		Memory mem = new Memory(rows, cols);
 		Debug.d(mem.toString());
 		
 		// Requirement 1: there should not be any onoccupied spaces.
 		for(int i = 0; i < rows; ++i) {
 			for(int j = 0; j < cols; ++j) {
				if(mem.getCard(i, j) == Memory.UNOCCUPIED) {
 					return false;
 				}
 			}
 		}
 		
 		// Requirement 2: There are exactly two of every card type(it is memory after all)
 		// And every card type is represented by a positive integer. 
 		
 		int[] cardCount = new int[mem.getNumPairs()];
 		
 		// count the number of every card type
 		for(int i = 0; i < rows; ++i) {
 			for(int j = 0; j < cols; ++j) {
 				cardCount[mem.getCard(i, j)]++;
 			}
 		}
 		
 		for(int i = 0; i < cardCount.length; ++i) {
 			if(cardCount[i] != 2)
 				return false;
 		}
 		
 		return true;
 	}
 	
 	public void testPlaceOutCards() {
 		/*
 		 * Since placeOutCards is based on Random numbers, it is quite difficult to rigorously test.
 		 * But the result of its calculations must fulfill certain requirements for it to 
 		 * be considered a success, so we will check for these requirements for a couple hundred 
 		 * randomly generated card placements. 
 		 */
 		
 		int TESTS = 30;
 		for(int i = 0; i < TESTS; ++i) {
 			
 			testPlaceOutCards(6,6);
 		}
 	}
 	
 	public void testGetCard() {
 		
 		
 		Memory mem = new Memory(4, 3);
 	
 		Debug.d(mem.toString());
 		
 		// we already knows that getCard(row, col) works. We are testing getCard(i)
 		
 		assertEquals(mem.getCard(0, 2), mem.getCard(2));
 		assertEquals(mem.getCard(1, 1), mem.getCard(4));
 
 		// the very last card
 		assertEquals(mem.getCard(3, 2), mem.getCard(11));
 	}
 }
