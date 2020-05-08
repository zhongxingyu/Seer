 package cz.fi.muni.pa165.calorycounter.backend.dto.convert;
 
 import cz.fi.muni.pa165.calorycounter.serviceapi.dto.AuthUserDto;
 import cz.fi.muni.pa165.calorycounter.backend.model.AuthUser;
 import org.springframework.stereotype.Component;
 
 /**
  * Conversion between AuthUser DTO and entity back and forth.
  *
  * @author Zdenek Lastuvka
  */

@Component
 public class AuthUserConvert {
 
     public static AuthUser fromDtoToEntity(AuthUserDto dto, String password) {
         if (dto == null) {
             throw new IllegalArgumentException("AuthUserConvert: fromDtoToEntity: null parameter!");
         }
         AuthUser authUser = new AuthUser();
         authUser.setId(dto.getUserId());
         authUser.setAge(dto.getAge());
         authUser.setName(dto.getName());
         authUser.setGender(dto.getSex());
         authUser.setUserRole(dto.getRole());
         authUser.setWeightCat(dto.getWeightCategory());
         authUser.setUsername(dto.getUsername());
         authUser.setPassword(password);
         return authUser;
     }
 
     public static AuthUserDto fromEntityToDto(AuthUser entity) {
         if (entity == null) {
             throw new IllegalArgumentException("AuthUserConvert: fromEntityToDto: null parameter!");
         }
         AuthUserDto authUserDto = new AuthUserDto();
         authUserDto.setUserId(entity.getId());
         authUserDto.setAge(entity.getAge());
         authUserDto.setName(entity.getName());
         authUserDto.setSex(entity.getGender());//proc ty renamy?
         authUserDto.setRole(entity.getUserRole());
         authUserDto.setWeightCategory(entity.getWeightCat());
         authUserDto.setUsername(entity.getUsername());
         return authUserDto;
     }
 }
