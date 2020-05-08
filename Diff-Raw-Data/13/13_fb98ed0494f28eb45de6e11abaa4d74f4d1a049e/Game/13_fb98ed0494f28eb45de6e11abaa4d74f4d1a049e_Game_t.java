 package no.niths.domain.battlestation;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import no.niths.common.AppConstants;
 import no.niths.domain.Domain;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 
 @XmlRootElement
 @Entity
 @Table(name = AppConstants.GAMES)
 @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
 public class Game implements Domain {
 
     @Transient
     private static final long serialVersionUID = 3498065037797521916L;
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;
 
     @Column
     @Size(min = 3, max = 80, message ="The length of the name must be between 3 to 80 letters")
     private String title;
 
     @Column
     @Enumerated(EnumType.STRING)
     private GameCategory category;
 
     @ManyToOne(fetch = FetchType.LAZY, targetEntity = Console.class)
     @JoinTable(name = "games_consoles",
            joinColumns = @JoinColumn(name = "games_id"),
            inverseJoinColumns = @JoinColumn(name = "consoles_id"))
     @Cascade(CascadeType.ALL)
     private Console console;
 
     public Game(){
         this(null, null, null);
         setConsole(null);
     }
 
     public Game(String name){
         setTitle(name);
     }
 
     public Game(String name, GameCategory category, Integer locker){
         setTitle(name);
         setCategory(category);
     }
 
     @Override
     public void setId(Long id) {
         this.id = id;
     }
 
     @Override
     public Long getId() {
         return id;
     }
 
     public void setTitle(String name) {
         this.title = name;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setCategory(GameCategory category) {
         this.category = category;
     }
 
     public GameCategory getCategory() {
         return category;
     }
 
     @JsonSerialize(as=Console.class)
     public Console getConsole() {
         return console;
     }
 
     public void setConsole(Console console) {
         this.console = console;
     }
 
     @Override
     public boolean equals(Object that) {
         if(!(that instanceof Game)) return false;
         Game game = (Game) that;
 
         return game == this || game.getId().equals(id);
     }
 
     @JsonIgnore
     public boolean isEmpty(){
         return (id == null && title == null && category == null);
     }
 
     @Override
     public String toString() {
         return String.format("[%s][%s][%s]", id, title, category);
     }
 }
