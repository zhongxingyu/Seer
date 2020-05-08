 package net.micwin.elysium.entities.replication;
 
 /*
  (c) 2012 micwin.net
 
  This file is part of open-space.
 
  open-space is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  open-space is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero Public License for more details.
 
  You should have received a copy of the GNU Affero Public License
  along with open-space.  If not, see http://www.gnu.org/licenses.
 
  Diese Datei ist Teil von open-space.
 
  open-space ist Freie Software: Sie können es unter den Bedingungen
  der GNU Affero Public License, wie von der Free Software Foundation,
  Version 3 der Lizenz oder (nach Ihrer Option) jeder späteren
  veröffentlichten Version, weiterverbreiten und/oder modifizieren.
 
  open-space wird in der Hoffnung, dass es nützlich sein wird, aber
  OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
  Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
  Siehe die GNU Affero Public License für weitere Details.
 
  Sie sollten eine Kopie der GNU Affero Public License zusammen mit diesem
  Programm erhalten haben. Wenn nicht, siehe http://www.gnu.org/licenses. 
 
  */
 import net.micwin.elysium.entities.appliances.Utilization;
 
 /**
  * A complexity calculator to compute complexity upon the assumption that each
  * element adds not only complexity to the whole. but raises complexity to add
  * another element.
  * 
  * @author MicWin
  * 
  */
 public class SimpleComplexityCalculator implements IComplexityCalculator {
 
 	private final long base;
 
 	public SimpleComplexityCalculator(long base) {
 		this.base = base;
 	}
 
 	@Override
 	public long calculateComplexity(BluePrint bluePrint) {
 
 		long componentCount = 0;
 		long complexitySum = 0;
 
 		for (Utilization utilization : bluePrint.getUtilizations()) {
 			componentCount += utilization.getCount();
			complexitySum += utilization.getAppliance().getBaseComplexity() * utilization.getLevel();
 		}
 
 		return Math.round(complexitySum * Math.pow(1.01, componentCount));
 	}
 
 }
