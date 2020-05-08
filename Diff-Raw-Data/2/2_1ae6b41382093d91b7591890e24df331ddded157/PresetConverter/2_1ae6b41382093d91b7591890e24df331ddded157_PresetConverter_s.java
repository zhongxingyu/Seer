 package com.atanor.smanager.domain.converter;
 
 import javax.inject.Inject;
 
 import org.apache.commons.lang3.Validate;
 
 import com.atanor.smanager.domain.entity.Preset;
 import com.atanor.smanager.rpc.dto.PresetDto;
 
 public class PresetConverter extends AbstractConverter<PresetDto, Preset> {
 
 	@Inject
 	private WindowConverter winConverter;
 
 	@Override
 	public PresetDto toDto(final Preset entity) {
 		Validate.notNull(entity, "entity param can not be null");
 
 		final PresetDto dto = new PresetDto(entity.getId());
 		dto.setWindows(convertEntityList(winConverter, entity.getWindows()));
 
 		return dto;
 	}
 
 	@Override
 	public Preset toEntity(final PresetDto dto) {
 		Validate.notNull(dto, "dto param can not be null");
 
		final Preset entity = new Preset();
 		entity.setWindows(convertDtoList(winConverter, dto.getWindows()));
 
 		return entity;
 	}
 }
