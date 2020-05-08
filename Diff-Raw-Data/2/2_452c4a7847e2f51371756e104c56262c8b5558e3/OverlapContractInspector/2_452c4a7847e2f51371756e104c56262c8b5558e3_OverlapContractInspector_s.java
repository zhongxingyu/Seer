 /*
  * This file is part of the aidGer project.
  *
  * Copyright (C) 2010-2013 The aidGer Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.aidger.model.inspectors;
 
 import static de.aidger.utils.Translation._;
 
 import java.text.MessageFormat;
 import java.util.Date;
 import java.util.List;
 
 import de.aidger.model.models.Assistant;
 import de.aidger.model.models.Contract;
 import de.aidger.view.forms.ContractEditorForm.ContractType;
 import de.aidger.view.models.UIAssistant;
 import siena.SienaException;
 
 /**
  * Inspector for overlapped new contracts.
  * 
  * @author aidGer Team
  */
 public class OverlapContractInspector extends Inspector {
 
     /**
      * The contract to be checked.
      */
     Contract contract;
 
     /**
      * Creates an overlap contract inspector.
      * 
      * @param contract
      *            the contract to be checked
      */
     public OverlapContractInspector(Contract contract) {
         this.contract = contract;
     }
 
     /*
      * Checks for oberlapped new contracts.
      */
     @Override
     public void check() {
         // do nothing if contract is not new
         if (ContractType.valueOf(contract.getType()) != ContractType.newContract) {
             return;
         }
 
         try {
             Assistant assistant = new Assistant((new Assistant())
                 .getById(contract.getAssistantId()));
 
             List<Contract> contracts = (new Contract()).getContracts(assistant);
 
             for (Contract other : contracts) {
                 // only handle new contracts and different one
                 if (ContractType.valueOf(other.getType()) != ContractType.newContract
                        || contract.getId().equals(other.getId())) {
                     continue;
                 }
 
                 Date s1 = contract.getStartDate();
                 Date e1 = contract.getEndDate();
 
                 Date s2 = other.getStartDate();
                 Date e2 = other.getEndDate();
 
                 if (s2.compareTo(e1) <= 0 && e2.compareTo(s1) >= 0) {
                     result = MessageFormat
                         .format(
                             _("The new contract of {0} overlaps with another one."),
                             new Object[] { new UIAssistant(assistant)
                                 .toString() });
 
                     return;
                 }
             }
         } catch (SienaException e) {
         }
     }
 }
