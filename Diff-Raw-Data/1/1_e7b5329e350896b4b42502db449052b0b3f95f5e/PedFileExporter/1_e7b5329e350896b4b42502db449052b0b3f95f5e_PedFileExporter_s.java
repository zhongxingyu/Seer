 package eu.wuttke.nrf.export;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
import eu.wuttke.nrf.domain.subject.PedFileLine;
 import eu.wuttke.nrf.domain.subject.Relation;
 import eu.wuttke.nrf.domain.subject.RelationRepository;
 import eu.wuttke.nrf.domain.subject.Subject;
 
 @Component
 public class PedFileExporter {
 
 	public String buildPedFileForFamily(Subject index) {
 		StringBuilder pedFile = new StringBuilder();
 		List<Relation> family = relationRepository.collectFamilySubjects(index);
 		for (Relation member : family) {
 			PedFileLine line = buildPedLineForMember(member);
 			line.setFamilyId("1");
 			line.appendStringLine(pedFile);
 		}
 		
 		return pedFile.toString();
 	}
 	
 	private PedFileLine buildPedLineForMember(Relation member) {
 		PedFileLine l = new PedFileLine();
 		l.setIndividualId(member.getSubject().getId().toString());
 		l.setGender(member.getSubject().getGender());
 		if (member.getMother() != null)
 			l.setMaternalId(member.getMother().getId().toString());
 		if (member.getFather() != null)
 			l.setPaternalId(member.getFather().getId().toString());
 		String pheno = "1";
 		if (member.getSubject().isDeath())
 			pheno = "X";
 		// kranke als Pheno 2 markieren?
 		l.setPhenotype(pheno);
 		return l;
 	}
 
 	public void setRelationRepository(RelationRepository relationRepository) {
 		this.relationRepository = relationRepository;
 	}
 	
 	@Autowired
 	private RelationRepository relationRepository;
 	
 }
