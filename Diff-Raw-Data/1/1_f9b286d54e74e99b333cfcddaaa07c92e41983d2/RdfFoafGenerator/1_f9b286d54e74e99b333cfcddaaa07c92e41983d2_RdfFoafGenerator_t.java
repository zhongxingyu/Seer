 package es.upm.dit.gsi.episteme;
 
 import java.util.List;
 
 public class RdfFoafGenerator {
 
 	private List<Person> people;
 	private String endpointResource = "http://localhost:8080/LMF-3.0.0/resource/";
 	
 	public RdfFoafGenerator(List<Person> people){
 		this.people = people;
 	}
 	
 	public String generatePerson(Person p){		
 		
 		String rdf = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n<rdf:RDF\n    " +
 				"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n    " +
 				"xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n    " +
 				"xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"\n    " +
 				"xmlns:vcard=\"http://www.w3.org/2006/vcard/ns#\"\n    " +
 				"xmlns:cv=\"http://kaste.lv/~captsolo/semweb/resume/cv.rdfs#\">\n    " +
 				"<foaf:Person rdf:about=\""+endpointResource+p.getUid()+"\">\n        " +
 				"<foaf:name>"+p.getName()+"</foaf:name>\n        " +
 				"<foaf:homepage rdf:resource=\"http://www.gsi.dit.upm.es/~"+p.getUid()+"\" />\n        " +
 				"<foaf:thumbnail rdf:resource=\""+p.getPhotoUrl()+"\" />\n        " +
 				"<vcard:streetAddress>"+p.getAddress()+"</vcard:streetAddress>\n        " +
 				"<vcard:locality>"+p.getLocality()+"</vcard:locality>\n        " +
 				"<vcard:postalCode>"+p.getZip()+"</vcard:postalCode>\n\t\t" +
 				"<cv:hasSkill>\n";
 				for(Skill skill : p.getSkills()){
 					rdf += "\t\t    <cv:Skill>\n\t\t\t    " +
 							"<cv:skillName rdf:resource=\""+endpointResource+"skills/"+skill.getUid()+"\" />\n\t\t            " +
 							"<cv:skillLevel rdf:resource=\""+skill.getLevel()+"\"/>\n\t\t    " +
 							"</cv:Skill>\n            ";
 				}				
 				rdf += "</cv:hasSkill>\n        " +
 				"<cv:conditionWillTravel>true</cv:conditionWillTravel>\n        " +
 				"<cv:cvDescription>"+p.getDescription()+"</cv:cvDescription>\n        " +
 				"<cv:targetSalary>2000</cv:targetSalary>\n    " +
 				"</foaf:Person>\n</rdf:RDF>";
 		
 		return rdf;
 	}
 	
 	public void generateAll(){
 		for(Person p : people){
 			String rdf = this.generatePerson(p);
 			System.out.println(rdf);
 		}
 	}
 
 }
