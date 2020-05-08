 package models;
 import javax.persistence.Entity;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Property;
 
 public class PropertiesTDB extends play.db.jpa.Model{
 
 	//classes
 	public Resource bolsa;
 	public Resource reflex;
 	public Resource aventura;
 	public Resource infantil;
 	public Resource objetiva;
 
 	//material_foto
 	public com.hp.hpl.jena.rdf.model.Property marca;
 	public com.hp.hpl.jena.rdf.model.Property preco;
 	public com.hp.hpl.jena.rdf.model.Property titulo;
 
 	//bolsas
 	public com.hp.hpl.jena.rdf.model.Property dimExt;
 	public com.hp.hpl.jena.rdf.model.Property dimInt;
 
 	//objetiva
 	public com.hp.hpl.jena.rdf.model.Property abertura;
 	public com.hp.hpl.jena.rdf.model.Property dimensoes;
 	public com.hp.hpl.jena.rdf.model.Property distFocal;
 	public com.hp.hpl.jena.rdf.model.Property peso;
 	public com.hp.hpl.jena.rdf.model.Property tipo;
 
 	//maquina
 	public com.hp.hpl.jena.rdf.model.Property bateria;
 	public com.hp.hpl.jena.rdf.model.Property dimensao;
 	public com.hp.hpl.jena.rdf.model.Property dimEcra;
 	public com.hp.hpl.jena.rdf.model.Property distFocalMaq;
 	public com.hp.hpl.jena.rdf.model.Property interf;
 	public com.hp.hpl.jena.rdf.model.Property pesoCorpo;
 	public com.hp.hpl.jena.rdf.model.Property resolucao;
 	public com.hp.hpl.jena.rdf.model.Property memoria;
 
 	//infantil
 	public com.hp.hpl.jena.rdf.model.Property cor;
 
 	//adulto
 	public com.hp.hpl.jena.rdf.model.Property formVid;
 	public com.hp.hpl.jena.rdf.model.Property fotoSeq;
 
 	//reflex
 	public com.hp.hpl.jena.rdf.model.Property nivUser;
 	public com.hp.hpl.jena.rdf.model.Property objIncl;
 
 	//aventura
 	public com.hp.hpl.jena.rdf.model.Property abertLente;
 	public com.hp.hpl.jena.rdf.model.Property iso;
 	public com.hp.hpl.jena.rdf.model.Property velo;
 	public com.hp.hpl.jena.rdf.model.Property zoom;
 
 	public PropertiesTDB(Model m, String NS) {
 
 		//classes
 		this.bolsa = m.getResource(NS + "Bolsas");
 		this.reflex = m.getResource(NS + "Reflex");
 		this.aventura = m.getResource(NS + "Aventura");
 		this.infantil = m.getResource(NS + "Infatil");
 		this.objetiva = m.getResource(NS + "Objetiva");
 
 		//material_foto
 		this.marca = m.getProperty(NS + "Marca");
 		this.preco = m.getProperty(NS + "Preco");
 		this.titulo = m.getProperty(NS + "Titulo");
 
 		//bolsas
 		this.dimExt = m.getProperty(NS + "Dim_Exterior");
 		this.dimInt = m.getProperty(NS + "Dim_Interior");
 
 		//objetiva
 		this.abertura = m.getProperty(NS + "Abertura");
 		this.dimensoes = m.getProperty(NS + "Dimensoes");
 		this.distFocal = m.getProperty(NS + "Dist_Focal");
 		this.peso = m.getProperty(NS + "Peso");
 		this.tipo = m.getProperty(NS + "Tipo");
 
 		//maquina
 		this.bateria = m.getProperty(NS + "Bateria");
 		this.dimensao = m.getProperty(NS + "Dimensao");
 		this.dimEcra = m.getProperty(NS + "Dimensao_ecra");
 		this.distFocalMaq = m.getProperty(NS + "Distancia_focal");
 		this.interf = m.getProperty(NS + "Interface");
 		this.pesoCorpo = m.getProperty(NS + "Peso_corpo");
 		this.resolucao = m.getProperty(NS + "Resolucao");
 		this.memoria = m.getProperty(NS + "Tipo_memoria");
 
 		//infantil
 		this.cor = m.getProperty(NS + "Cor");
 
 		//adulto
 		this.formVid = m.getProperty(NS + "Formato_Video");
 		this.fotoSeq = m.getProperty(NS + "Foto_sequencia");
 
 		//reflex
 		this.nivUser = m.getProperty(NS + "Nivel_utilizador");
 		this.objIncl = m.getProperty(NS + "Objetiva_incluida");
 
 		//aventura
 		this.abertLente = m.getProperty(NS + "Abertura_lente");
 		this.iso = m.getProperty(NS + "ISO");
 		this.velo = m.getProperty(NS + "Velocidade");
 		this.zoom = m.getProperty(NS + "Zoom");
 
 	}
 
 }
