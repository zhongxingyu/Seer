 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Persistence.inMemory;
 
 import Model.IncomeType;
 import Persistence.Interfaces.IIncomeTypeRepository;
import eapli.exception.EmptyList;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Classe Repositorio em memoria de tipos de rendimentos
  *
  * @autor 1110186 & 1110590
  */
 public class IncomeTypeRepository implements IIncomeTypeRepository{
 
     private static List<IncomeType> listIncomeType = new ArrayList<IncomeType>();
     
     public IncomeTypeRepository(){}
     
     @Override
     public void save(IncomeType intType) {
         
         if(intType == null){
             throw new IllegalArgumentException();
         }
         listIncomeType.add(intType);
     }
     
    public List<IncomeType> getAllIncomeType() throws EmptyList {
        List<IncomeType> aIncType = listIncomeType;
        if(aIncType.isEmpty()){
            throw new EmptyList("Exception EmptyList");
        }
        
        return aIncType;
    }
    
 }
