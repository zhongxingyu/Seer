 package spj.database.program;
 
 import com.google.common.collect.ImmutableList;
 import spj.database.SpjDomainMap;
 import spj.database.SpjDomainModelEntity;
 import spj.database.SpjDomainModelMapper;
 import spj.database.glavniprogram.GlavniProgramEntity;
 import spj.shared.domain.GlavniProgram;
 import spj.shared.domain.Program;
 import spj.shared.domain.SpjDomainModel;
 import spj.shared.util.SpjAssert;
 import spj.util.CastTool;
 
 import java.util.List;
 
 public class ProgramMapper implements SpjDomainModelMapper{
 
 
     @Override
     public <T extends SpjDomainModel> List<T> createDomainModel(Iterable<? extends SpjDomainModelEntity> spjDomainModelEntityList) {
         SpjAssert.isNotNull(spjDomainModelEntityList, "spjDomainModelEntityList");
 
         ImmutableList.Builder<T> resultBuilder = ImmutableList.builder();
 
         for (SpjDomainModelEntity spjDomainModelEntity : spjDomainModelEntityList)
         {
             T spjDomainModel = createDomainModel(spjDomainModelEntity);
             resultBuilder.add(spjDomainModel);
         }
 
         return resultBuilder.build();    }
 
     @Override
     public <T extends SpjDomainModel> T createDomainModel(SpjDomainModelEntity spjDomainModelEntity) {
         SpjAssert.isNotNull(spjDomainModelEntity, "spjDomainModelEntity");
 
         ProgramEntity programEntity = CastTool.cast(spjDomainModelEntity);
 
         GlavniProgram glavniProgram = SpjDomainMap.GLAVNI_PROGRAM.getMapper().createDomainModel(programEntity.getGlavniProgramEntity());
 
         return CastTool.cast(Program.builder()
                 .id(programEntity.getId())
                 .naziv(programEntity.getNaziv())
                 .zakonskaOsnova(programEntity.getZakonskaOsnova())
                 .brojZaposlenih(programEntity.getBrojZaposlenih())
                 .glavniProgram(glavniProgram)
                 .opis(programEntity.getOpis())
                 .opciCilj(programEntity.getOpciCilj())
                 .pokazateljUspijeha(programEntity.getPokazateljUspijeha())
                 .build());    }
 
     @Override
     public <T extends SpjDomainModelEntity> T createEntity(SpjDomainModel spjDomainModel) {
         SpjAssert.isNotNull(spjDomainModel, "spjDomainModel");
 
         Program program = CastTool.cast(spjDomainModel);
 
         GlavniProgramEntity glavniProgramEntity = SpjDomainMap.GLAVNI_PROGRAM.getMapper().createEntity(program.getGlavniProgram());
 
         ProgramEntity programEntity = new ProgramEntity();
         programEntity.setId(program.getId());
         programEntity.setNaziv(program.getNaziv());
         programEntity.setZakonskaOsnova(program.getZakonskaOsnova());
         programEntity.setBrojZaposlenih(program.getBrojZaposlenih());
         programEntity.setGlavniProgramEntity(glavniProgramEntity);
         programEntity.setOpis(program.getOpis());
         programEntity.setOpciCilj(program.getOpciCilj());
         programEntity.setPokazateljUspijeha(program.getPokazateljUspijeha());
 
        return CastTool.cast(programEntity);
     }
 }
