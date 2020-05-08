 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pa165.pujcovnastroju.converter;
 
 import cz.muni.fi.pa165.pujcovnastroju.dto.LoanDTO;
 import cz.muni.fi.pa165.pujcovnastroju.entity.Loan;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author xguttner
  */
 public class LoanDTOConverter {
     
     /**
      * Converts DTO to entity object
      * 
      * @param loanDTO to be converted
      * @return Loan object or null if loanDTO is null
      */
     public static Loan dtoToEntity(LoanDTO loanDTO) {
 	if (loanDTO == null) return null;
 	
 	Loan loan = new Loan();
 	loan.setId(loanDTO.getId());
 	loan.setCustomer(SystemUserDTOConverter.dtoToEntity(loanDTO.getCustomer()));
 	loan.setLoanTime(loanDTO.getLoanTime());
 	loan.setReturnTime(loanDTO.getReturnTime());
 	loan.setLoanState(LoanStateEnumDTOConverter.dtoToEntity(loanDTO.getLoanState()));
 	loan.setMachines(MachineDTOConverter.listToEntities(loanDTO.getMachines()));
 	return loan;
     }
     
     /**
      * Converts entity object to DTO
      * 
      * @param loan to be converted
      * @return LoanDTO or null if loan is null
      */
     public static LoanDTO entityToDTO(Loan loan) {
	if (loan == null) return null;
	
 	LoanDTO loanDTO = new LoanDTO();
 	loanDTO.setId(loan.getId());
 	loanDTO.setCustomer(SystemUserDTOConverter.entityToDTO(loan.getCustomer()));
 	loanDTO.setLoanTime(loan.getLoanTime());
 	loanDTO.setReturnTime(loan.getReturnTime());
 	loanDTO.setLoanState(LoanStateEnumDTOConverter.entityToDto(loan.getLoanState()));
 	loanDTO.setMachines(MachineDTOConverter.listToDto(loan.getMachines()));
 	return loanDTO;
     }
     
     /**
      * Converts list of DTOs to list of entity objects
      * 
      * @param loanDTOs to be converted
      * @return list of Loan or null if loanDTOs is null
      */
     public static List<Loan> listToEntities(List<LoanDTO> loanDTOs) {
 	if (loanDTOs == null) return null;
 	
 	List<Loan> loans = new ArrayList<>();
 	for (LoanDTO loanDTO : loanDTOs) {
 	    loans.add(LoanDTOConverter.dtoToEntity(loanDTO));
 	}
 	return loans;
     }
     
     /**
      * Converts list of entity objects to list of DTOs
      * 
      * @param loans to be converted
      * @return list of DTOs or null if loans is null
      */
     public static List<LoanDTO> listToDTOs(List<Loan> loans) {
 	if (loans == null) return null;
 	
 	List<LoanDTO> loanDTOs = new ArrayList<>();
 	for (Loan loan : loans) {
 	    loanDTOs.add(LoanDTOConverter.entityToDTO(loan));
 	}
 	return loanDTOs;
     }
 }
