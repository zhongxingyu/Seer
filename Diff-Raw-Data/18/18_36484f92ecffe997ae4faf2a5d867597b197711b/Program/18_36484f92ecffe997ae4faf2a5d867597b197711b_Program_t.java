 package spj.shared.domain;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.ImmutableList;
 import spj.shared.util.SpjAssert;
 import spj.shared.util.SpjEvaluate;
 
 import java.util.List;
 
 public class Program extends SpjCodebookModel
 {
     private final String zakonskaOsnova;
     private final String opis;
     private final String opciCilj;
     private final GlavniProgram glavniProgram;
     private final Integer brojZaposlenih;
     private final String pokazateljUspijeha;
 
     private Program(Long id, String naziv, String zakonskaOsnova, String opis, String opciCilj, GlavniProgram glavniProgram, Integer brojZaposlenih, String pokazateljUspijeha)
     {
         super(id, naziv);
         this.zakonskaOsnova = zakonskaOsnova;
         this.opis = opis;
         this.opciCilj = opciCilj;
         this.glavniProgram = glavniProgram;
         this.brojZaposlenih = brojZaposlenih;
         this.pokazateljUspijeha = pokazateljUspijeha;
     }
 
     public String getZakonskaOsnova()
     {
         return zakonskaOsnova;
     }
 
     public String getOpis()
     {
         return opis;
     }
 
     public String getOpciCilj()
     {
         return opciCilj;
     }
 
     public GlavniProgram getGlavniProgram()
     {
         return glavniProgram;
     }
 
     public Integer getBrojZaposlenih()
     {
         return brojZaposlenih;
     }
 
     public String getPokazateljUspijeha()
     {
         return pokazateljUspijeha;
     }
 
     @Override
     public boolean equals(Object otherAsObject)
     {
 
         if (this == otherAsObject)
         {
             return true;
         }
         if (otherAsObject == null)
         {
             return false;
         }
 
         if (getClass() != otherAsObject.getClass())
         {
             return false;
         }
 
         Program other = (Program) otherAsObject;
 
         return Objects.equal(this.id, other.id) &&
                 Objects.equal(this.naziv, other.naziv) &&
                 Objects.equal(this.zakonskaOsnova, other.zakonskaOsnova) &&
                 Objects.equal(this.opis, other.opis) &&
                 Objects.equal(this.opciCilj, other.opciCilj) &&
                 Objects.equal(this.glavniProgram, other.glavniProgram) &&
                 Objects.equal(this.brojZaposlenih, other.brojZaposlenih) &&
                 Objects.equal(this.pokazateljUspijeha, other.pokazateljUspijeha);
     }
 
     @Override
     public int hashCode()
     {
         return Objects.hashCode(this.id, this.naziv, this.zakonskaOsnova, this.opis, this.opciCilj, this.glavniProgram, this.brojZaposlenih, this.pokazateljUspijeha);
     }
 
     public static Program.Builder builder()
     {
         return new Program.Builder();
     }
 
     public static Program.Builder builder(Program program)
     {
         SpjAssert.isNotNull(program, "program");
 
         Program.Builder builder = new Program.Builder();
         builder.id(program.getIdString());
         builder.naziv(program.getNaziv());
         builder.zakonskaOsnova(program.getZakonskaOsnova());
         builder.opis(program.getOpis());
         builder.opciCilj(program.getOpciCilj());
         builder.glavniProgram(program.getGlavniProgram());
         builder.pokazateljUspijeha(program.getPokazateljUspijeha());
 
         if(SpjEvaluate.isNotNull(program.getBrojZaposlenih()))
             builder.brojZaposlenih(program.getBrojZaposlenih().toString());
 
        return builder;
     }
 
     public static class Builder extends SpjCodebookModel.Builder
     {
         private String zakonskaOsnova;
         private String opis;
         private String opciCilj;
         private GlavniProgram glavniProgram;
         private Integer brojZaposlenih;
         private String pokazateljUspijeha;
 
         @Override
         public Program.Builder id(String id)
         {
             SpjAssert.isNotNull(id, "id");
 
             if (!id.isEmpty())
             {
                 this.id = Long.valueOf(id);
             }
 
             return this;
         }
 
         @Override
         public Program.Builder naziv(String naziv)
         {
             SpjAssert.isNotNull(naziv, "naziv");
 
             this.naziv = naziv;
 
             return this;
         }
 
         public Program.Builder zakonskaOsnova(String zakonskaOsnova)
         {
             SpjAssert.isNotNull(zakonskaOsnova, "zakonskaOsnova");
 
             this.zakonskaOsnova = zakonskaOsnova;
 
             return this;
         }
 
         public Program.Builder opis(String opis)
         {
             SpjAssert.isNotNull(opis, "opis");
 
             this.opis = opis;
 
             return this;
         }
 
         public Program.Builder opciCilj(String opciCilj)
         {
             SpjAssert.isNotNull(opciCilj, "opciCilj");
 
             this.opciCilj = opciCilj;
 
             return this;
         }
 
         public Program.Builder glavniProgram(GlavniProgram glavniProgram)
         {
             SpjAssert.isNotNull(glavniProgram, "glavniProgram");
 
             this.glavniProgram = glavniProgram;
 
             return this;
         }
 
         public Program.Builder brojZaposlenih(String brojZaposlenih)
         {
             SpjAssert.isNotNull(brojZaposlenih, "brojZaposlenih");
 
             if (!brojZaposlenih.isEmpty())
             {
                 this.brojZaposlenih = Integer.valueOf(brojZaposlenih);
             }
 
             return this;
         }
 
         public Program.Builder pokazateljUspijeha(String pokazateljUspijeha)
         {
             SpjAssert.isNotNull(pokazateljUspijeha, "pokazateljUspijeha");
 
             this.pokazateljUspijeha = pokazateljUspijeha;
 
             return this;
         }
 
         public Program build()
         {
             if (!validate().isEmpty())
             {
                 throw new IllegalStateException("Trying to build invalid Program");
             }
 
             return new Program(id, naziv, zakonskaOsnova, opis, opciCilj, glavniProgram, brojZaposlenih, pokazateljUspijeha);
         }
 
         @Override
         public List<String> validate()
         {
             ImmutableList.Builder<String> resultBuilder = ImmutableList.builder();
 
             if (!SpjEvaluate.isNotNullNorEmpty(this.naziv))
             {
                 resultBuilder.add("Naziv programa ne smije biti prazan");
             }
 
             if(!SpjEvaluate.isNotNull(this.glavniProgram))
                 resultBuilder.add("Program mora pripadati glavnom programu!");
 
             if (!SpjEvaluate.isGreaterThan(this.id, 999L) || !SpjEvaluate.isLesserThan(this.id, 10000L))
             {
                 resultBuilder.add("Sifra programa mora biti pozitivan cetveroznamenkasti broj!");
             }
 
             if (SpjEvaluate.isNotNull(this.brojZaposlenih) && !SpjEvaluate.isGreaterThan(this.brojZaposlenih, 0))
             {
                 resultBuilder.add("BrojZaposlenih programa mora biti pozitivan broj!");
             }
 
             return resultBuilder.build();
         }
 
         @Override
         public Program defaultValue()
         {
             return new Program(0L, "", "", "", "", GlavniProgram.builder().defaultValue(), 0, "");
         }
     }
 }
