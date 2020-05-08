 package cz.muni.fi.pa165.pujcovnastroju.converter;
 
 import cz.muni.fi.pa165.pujcovnastroju.dto.MachineTypeEnumDTO;
 import cz.muni.fi.pa165.pujcovnastroju.entity.MachineTypeEnum;
 
 /**
  * Basic Machine type enumeration DTO converter
  * 
  * @author Michal Merta 374015
  * 
  */
 public class MachineTypeDTOConverter {
 	public static MachineTypeEnum dtoToEntity(MachineTypeEnumDTO dto) {
 		if (dto == null)
 			return null;
 
		MachineTypeEnum type = MachineTypeEnum.valueOf(dto.typeLabel);
 		return type;
 	}
 
 	public static MachineTypeEnumDTO entityToDto(MachineTypeEnum type) {
 		if (type == null)
 			return null;
 
 		MachineTypeEnumDTO dto = new MachineTypeEnumDTO();
 		dto.setId(Long.valueOf(type.ordinal()));
 		dto.setTypeLabel(type.name());
 		return dto;
 	}
 }
