 package is2;
 
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.SequenceGenerator;
 
 @Entity
 public class CategoriaNota {
         
         @Id
         @SequenceGenerator(name = "categoria_nota_id_gen", allocationSize = 1)
         @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "categoria_nota_id_gen")
         private Long id;
         
         private String nombre;
         
         @OneToMany(mappedBy="categoria")
         private List<Nota> notas;
 
         public String getNombre() {
                 return nombre;
         }
 
         public void setNombre(String nombre) {
                 this.nombre = nombre;
         }
         
 }
