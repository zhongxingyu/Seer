 /*Copyright (C) 2012 Lars Andersen, Tormund S. Haus.
 larsan@stud.ntnu.no
 tormunds@ntnu.no
 
 EASY is free software: you can redistribute it and/or modify it
 under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
  
 EASY is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.
  
 You should have received a copy of the GNU General Public License
     along with EASY.  If not, see <http://www.gnu.org/licenses/>.*/
 package edu.ntnu.EASY.selection.adult;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import edu.ntnu.EASY.Population;
 import edu.ntnu.EASY.individual.Individual;
 
 public class GenerationalMixing<PType> implements AdultSelector<PType> {
 	
 	private int numAdults;
 
 	public GenerationalMixing( int numAdults) {
 		this.numAdults = numAdults;
 	}

	@Override
	public List<Individual<?, PType>> select(List<Individual<?, PType>> adults,	List<Individual<?, PType>> children) {
		List<Individual<?, PType>> population = new LinkedList<Individual<?, PType>>();
		population.addAll(children);
		population.addAll(adults);
		Collections.sort(population );
		int numToDrop = population.size() - numAdults;
		return population.subList(numToDrop, population.size());
	}

 	@Override
	public <GType> Population<?, PType> select(Population<GType, PType> adults,	Population<?, PType> children) {
 		adults.addAll(children);
 		adults.drop(adults.size() - numAdults); 
 		return adults;
 	}
 }
