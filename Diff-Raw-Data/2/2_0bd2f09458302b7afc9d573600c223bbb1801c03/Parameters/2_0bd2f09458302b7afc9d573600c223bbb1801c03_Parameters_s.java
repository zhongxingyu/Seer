 /*===========================================================================
   Copyright (C) 2008-2010 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ============================================================================*/
 
 package net.sf.okapi.filters.tmx;
 
 import net.sf.okapi.common.BaseParameters;
 import net.sf.okapi.common.ParametersDescription;
 import net.sf.okapi.common.encoder.XMLEncoder;
 
 public class Parameters extends BaseParameters{
 
 	//--TODO change back to protected when the testing setup is completed-- 
 	public boolean processAllTargets;
 	public boolean consolidateDpSkeleton;
 	public boolean escapeGT;
 
 	public Parameters () {
 		reset();
 	}	
 
 	public void reset() {
 		escapeGT = false;
 		processAllTargets=true;
 		consolidateDpSkeleton=true;
 	}
 	
 	public boolean getProcessAllTargets () {
 		return processAllTargets;
 	}
 
 	public void setProcessAllTargets (boolean processAllTargets) {
 		this.processAllTargets = processAllTargets;
 	}
 
 	public boolean getConsolidateDpSkeleton() {
 		return consolidateDpSkeleton;
 	}
 
 	public void setConsolidateDpSkeleton (boolean consolidateDpSkeleton) {
 		this.consolidateDpSkeleton = consolidateDpSkeleton;
 	}
 
 	public boolean getEscapeGT () {
 		return escapeGT;
 	}
 
 	public void setEscapeGT (boolean escapeGT) {
 		this.escapeGT = escapeGT;
 	}
 
 	public void fromString(String data) {
 		reset();
 		buffer.fromString(data);
 		escapeGT = buffer.getBoolean(XMLEncoder.ESCAPEGT, escapeGT);
 		processAllTargets = buffer.getBoolean("processAllTargets", processAllTargets);
 		consolidateDpSkeleton = buffer.getBoolean("consolidateDpSkeleton", consolidateDpSkeleton);
 	}
 
 	public String toString () {
 		buffer.reset();
 		buffer.setBoolean(XMLEncoder.ESCAPEGT, escapeGT);		
 		buffer.setBoolean("processAllTargets", processAllTargets);
 		buffer.setBoolean("consolidateDpSkeleton", consolidateDpSkeleton);
 		return buffer.toString();
 	}
 
 	@Override
 	public ParametersDescription getParametersDescription () {
 		ParametersDescription desc = new ParametersDescription(this);
 		desc.add(XMLEncoder.ESCAPEGT, "Escape the greater-than characters", null);
		desc.add("processAllTargets", "Read all traget entries", null);
 		desc.add("consolidateDpSkeleton", "Group all document parts skeleton into one", null);
 		return desc;
 	}
 
 }
