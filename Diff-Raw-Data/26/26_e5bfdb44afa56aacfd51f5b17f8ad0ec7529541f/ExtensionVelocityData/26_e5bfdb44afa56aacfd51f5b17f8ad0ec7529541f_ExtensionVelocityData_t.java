 /*****************************************************************************
  * This source file is part of SBS (Screen Build System),                    *
  * which is a component of Screen Framework                                  *
  *                                                                           *
  * Copyright (c) 2008-2013 Ratouit Thomas                                    *
  *                                                                           *
  * This program is free software; you can redistribute it and/or modify it   *
  * under the terms of the GNU Lesser General Public License as published by  *
  * the Free Software Foundation; either version 3 of the License, or (at     *
  * your option) any later version.                                           *
  *                                                                           *
  * This program is distributed in the hope that it will be useful, but       *
  * WITHOUT ANY WARRANTY; without even the implied warranty of                *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser   *
  * General Public License for more details.                                  *
  *                                                                           *
  * You should have received a copy of the GNU Lesser General Public License  *
  * along with this program; if not, write to the Free Software Foundation,   *
  * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA, or go to   *
  * http://www.gnu.org/copyleft/lesser.txt.                                   *
  *****************************************************************************/
 
 package com.thoratou.exact.processors;
 
public class ExtensionVelocityData {
     private PathStep extensionStep = null;
     private PathStep.Kind  filterKind = null;
 
    public PathStep getExtensionStep() {
         return extensionStep;
     }
 
    public void setExtensionStep(PathStep extensionStep) {
         this.extensionStep = extensionStep;
     }
 
    public PathStep.Kind getFilterKind() {
         return filterKind;
     }
 
    public void setFilterKind(PathStep.Kind filterKind) {
         this.filterKind = filterKind;
     }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("{extension,");
        stringBuffer.append(filterKind.name());
        stringBuffer.append(",");
        stringBuffer.append(extensionStep);
        stringBuffer.append("}");
        return stringBuffer.toString();
    }
 }
