 /**
  *
  */
 package com.jcertif.service.impl.participant;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang.RandomStringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.jcertif.bo.participant.Participant;
 import com.jcertif.bo.participant.RoleParticipant;
 import com.jcertif.bo.participant.TypeParticipant;
 import com.jcertif.dao.api.participant.ParticipantDAO;
 import com.jcertif.dao.api.participant.ProfilUtilisateurDAO;
 import com.jcertif.dao.api.participant.RoleParticipantDAO;
 import com.jcertif.dao.api.participant.TypeParticipantDAO;
 import com.jcertif.exception.ExistingEmailException;
 import com.jcertif.service.AbstractService;
 import com.jcertif.service.api.participant.ParticipantService;
 import com.jcertif.service.mail.CSenderService;
 
 /**
  * Implementation of the {@link ParticipantService}.
  * 
  * @author Bernard Adanlessossi
  */
 @Service
 public class ParticipantServiceImpl extends AbstractService<Participant, Long, ParticipantDAO>
 		implements ParticipantService {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantServiceImpl.class);
 	@Autowired
 	private ParticipantDAO participantDAO;
 	@Autowired
 	private RoleParticipantDAO roleParticipantDAO;
 	@Autowired
 	private TypeParticipantDAO typeParticipantDAO;
 	@Autowired
 	private ProfilUtilisateurDAO profilUtilisateurDAO;
 	@Autowired
 	private CSenderService cSenderService;
 
 	@Override
 	public ParticipantDAO getDAO() {
 		return participantDAO;
 	}
 
 	@Override
 	public void setDAO(ParticipantDAO participantDAO) {
 		this.participantDAO = participantDAO;
 	}
 
 	@Override
 	@Transactional
 	public Participant save(Participant entite) {
 		List<Participant> partList = participantDAO.find(entite.getEmail(), entite.getConference()
 				.getId());
 		if (partList.size() != 0) {
 			throw new ExistingEmailException();
 		}
 
 		// Setting default inscription date
 		entite.setDateInscription(Calendar.getInstance());
 
 		updateRoleParticipantIfNeeded(entite);
 		updateTypeParticipantIfNeeded(entite);
 
 		if (entite.getProfilUtilisateur() != null) {
 
 			// Synchronisation de l'adresse email
 			if (entite.getProfilUtilisateur().getEmail() != null) {
 				entite.setEmail(entite.getProfilUtilisateur().getEmail());
 			}
 
 			// Cryptage du mot de passe en MD5
 			String key = entite.getProfilUtilisateur().getPassword();
 			if (key != null) {
 				entite.getProfilUtilisateur().setPassword(getEncodedPassword(key));
 			}
 
 			profilUtilisateurDAO.persist(entite.getProfilUtilisateur());
 		}
 		return super.save(entite);
 
 	}
 
 	private void updateTypeParticipantIfNeeded(Participant entity) {
 		if (entity.getTypeParticipant() != null && entity.getTypeParticipant().getId() == null) {
 			List<TypeParticipant> types = typeParticipantDAO.findByProperty("code", entity
 					.getTypeParticipant().getCode());
 			if (types.size() > 1) {
 				LOGGER.warn("Il y a plusieurs TypeParticipant avec le code {}", entity
 						.getTypeParticipant().getCode());
 			}
 			if (!types.isEmpty()) {
 				entity.setTypeParticipant(types.iterator().next());
 			}
 		}
 	}
 
 	private void updateRoleParticipantIfNeeded(Participant entity) {
 		if (entity.getTypeParticipant() != null && entity.getTypeParticipant().getId() == null) {
 			List<RoleParticipant> roles = roleParticipantDAO.findByProperty("code", entity
 					.getRoleparticipant().getCode());
 			if (roles.size() > 1) {
 				LOGGER.warn("Il y a plusieurs RoleParticipant avec le code {}", entity
 						.getRoleparticipant().getCode());
 			}
 			if (!roles.isEmpty()) {
 				entity.setRoleparticipant(roles.iterator().next());
 			}
 		}
 
 	}
 
 	public String getEncodedPassword(String key) {
 		byte[] uniqueKey = key.getBytes();
 		byte[] hash = null;
 		try {
 			hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 		StringBuilder hashString = new StringBuilder();
 		for (int i = 0; i < hash.length; ++i) {
 			hashString.append(hash[i]);
 		}
 		return hashString.toString();
 	}
 
 	@Override
 	public Participant update(Participant entite) {
 		return super.update(entite);
 	}
 
 	@Override
 	public List<Participant> findAllWithProposition() {
 		List<Participant> participants = participantDAO.findAll();
 
 		if (participants != null && !participants.isEmpty()) {
 			for (Participant participant : participants) {
 
 				if (participant.getPropositionPresentations() != null
 						&& !participant.getPropositionPresentations().isEmpty()) {
 					participant.getPropositionPresentations().iterator().next();
 				}
 			}
 		}
 		return participants;
 	}
 
 	/**
 	 * @param fileStream
 	 * @param idParticipant
 	 * @param codeRole
 	 * @param ext
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	@Override
 	public void saveInFile(final InputStream fileStream, Long idParticipant, String codeRole,
 			String ext) throws IOException {
 		File dir = new File("./" + codeRole);
 		if (!dir.exists()) {
 			dir.mkdir();
 		}
 
 		File outputFile = new File(dir, idParticipant + "." + ext);
 
 		OutputStream out;
 
 		out = new FileOutputStream(outputFile);
 
 		byte buf[] = new byte[1024];
 		int len;
 
 		while ((len = fileStream.read(buf)) > 0) {
 			out.write(buf, 0, len);
 		}
 		out.close();
 		fileStream.close();
 
 		LOGGER.info("File {} saved for idParticipant={}", outputFile.getAbsolutePath(),
 				idParticipant);
 	}
 
 	@Override
 	public Participant connect(String email, String password, Long conferenceid) {
 		Set<Participant> partList = new HashSet<Participant>(participantDAO.find(email,
 				conferenceid));
 		if (partList.size() > 1) {
 			throw new RuntimeException(
 					"Plusieurs personnes avec la mme adresse email  la mme conference " + email);
 		}
 
 		Participant participant = null;
 
 		if (!partList.isEmpty()) {
 			Participant participantBase = partList.iterator().next();
 
 			if (getEncodedPassword(password).equals(
 					participantBase.getProfilUtilisateur().getPassword())) {
 				participant = participantBase;
 			}
 		}
 
 		return participant;
 	}
 
 	@Override
 	@Transactional
 	public Participant generateNewPassword(String email, Long conferenceId) {
 		List<Participant> participants = participantDAO.find(email, conferenceId);
 
 		Participant participant = new Participant();
 		if (!participants.isEmpty()) {
 			participant = participants.iterator().next();
 			String newPassword = RandomStringUtils.random(12, true, false);
 			participant.getProfilUtilisateur().setPassword(getEncodedPassword(newPassword));
 			participant = participantDAO.merge(participant);
 			cSenderService.sendNewPassword(participant, newPassword);
 		}
 
 		return participant;
 	}
 
 	@Override
 	@Transactional
 	public Participant updateBio(String bio, String email, Long conferenceId) {
 		List<Participant> participants = participantDAO.find(email, conferenceId);
 		Participant participant = participants.iterator().next();
 		participant.setDetails(bio);
 		participant = participantDAO.merge(participant);
 		return participant;
 	}
 
 	@Override
 	public Participant findByEmail(String email, Long conferenceId) {
 		List<Participant> participants = participantDAO.find(email, conferenceId);
		if (participants.isEmpty()) {
			return null;
		}
 		return participants.iterator().next();
 	}
 }
