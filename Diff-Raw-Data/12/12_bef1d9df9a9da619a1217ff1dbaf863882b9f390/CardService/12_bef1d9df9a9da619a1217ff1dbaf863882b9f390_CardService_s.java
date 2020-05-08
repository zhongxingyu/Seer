 /**
  * Copyright (c) 2011 SC The Red Point SA. All rights reserved.
  */
 package mage.tracker.service;
 
 import java.math.BigInteger;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaBuilder.In;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Join;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import mage.tracker.domain.Account;
 import mage.tracker.domain.Card;
 import mage.tracker.domain.Card_;
 import mage.tracker.domain.CardEdition;
 import mage.tracker.domain.CardEdition_;
 import mage.tracker.domain.CardRarity;
 import mage.tracker.domain.CardStatus;
 import mage.tracker.domain.CardStatus_;
 import mage.tracker.domain.Expansion;
 import mage.tracker.domain.Expansion_;
 import mage.tracker.dto.CardCriteria;
 import mage.tracker.dto.ExpansionStatus;
 import mage.tracker.repository.AccountRepository;
 import mage.tracker.repository.CardEditionRepository;
 import mage.tracker.repository.CardRepository;
 import mage.tracker.repository.ExpansionRepository;
 import mage.tracker.repository.GenericRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * 
  * @author North
  */
 @Service
 @Transactional(readOnly = true)
 public class CardService {
 
     @PersistenceContext
     private EntityManager em;
     @Autowired
     @Qualifier("accountRepository")
     private AccountRepository accountRepository;
     @Autowired
     @Qualifier("cardRepository")
     private CardRepository cardRepository;
     @Autowired
     @Qualifier("cardEditionRepository")
     private CardEditionRepository cardEditionRepository;
     @Autowired
     @Qualifier("expansionRepository")
     private ExpansionRepository expansionRepository;
     @Autowired
     @Qualifier("genericRepository")
     private GenericRepository<CardStatus> cardStatusRepository;
 
     public List<Expansion> getExpansions() {
         return expansionRepository.findAll();
     }
 
     @Transactional(readOnly = false)
     public Expansion saveExpansion(Expansion expansion) {
         Expansion result = expansionRepository.findByName(expansion.getName());
         if (result == null) {
             return expansionRepository.persist(expansion);
         }
         return result;
     }
 
     @Transactional(readOnly = false)
     public Boolean saveAccount(Account account) {
         Account result = accountRepository.findByName(account.getName());
         if (result == null) {
             accountRepository.persist(account);
             return true;
         }
         return false;
     }
 
     public Account authenticateAccount(String name, String password) {
         Account account = accountRepository.findByName(name);
         if (account != null && account.getPassword().equals(password)) {
             return account;
         }
         return null;
     }
 
     @Transactional(readOnly = false)
     public boolean updateCardEditionMtgoImageId(String cardData) {
         String[] cardAttributes = cardData.split("\\|");
         CardEdition edition = cardEditionRepository.findByNameAndExpansionCode(cardAttributes[1], cardAttributes[0]);
         if (edition != null) {
             edition.setMtgoImageId(cardAttributes[3]);
             cardEditionRepository.persist(edition);
             return true;
         }
 
         return false;
     }
 
     /**
      * Used to add a new card or update its data if it already exists
      *
      * @param cardData
      */
     @Transactional(readOnly = false)
     public void updateCardData(String cardData) {
         String[] cardAttributes = cardData.split("\\|");
         Card card = cardRepository.findByName(cardAttributes[1]);
         if (card == null) {
             card = new Card();
             card.setName(cardAttributes[1]);
 
             String[] cardType = cardAttributes[4].split(" - ");
             card.setType(cardType[0]);
             if (cardType.length > 1) {
                 card.setSubType(cardType[1]);
             }
 
             card.setCost(cardAttributes[2]);
             try {
                 card.setCmc(Integer.parseInt(cardAttributes[3]));
             } catch (NumberFormatException e) {
                 card.setCmc(0);
             }
             String[] pt = cardAttributes[7].split(" / ");
             if (pt.length == 2) {
                 card.setPower(pt[0]);
                 card.setToughness(pt[1]);
             }
             // TODO: Add loyalty
             if (pt.length == 1 && pt[0].length() > 0) {
                 card.setToughness(pt[0]);
             }
 
             CardStatus cardStatus = new CardStatus();
             cardStatus = cardStatusRepository.persist(cardStatus);
             card.setStatus(cardStatus);
 
             card.setAbilities(cardAttributes[5]);
 
             cardRepository.persist(card);
         } else if (!card.getAbilities().equals(cardAttributes[5])) {
             card.setAbilities(cardAttributes[5]);
             cardRepository.merge(card);
         }
 
         Expansion expansion = expansionRepository.findByName(cardAttributes[8]);
         if (card != null && expansion != null) {
             if (cardEditionRepository.findByNameAndExpansion(card.getName(), expansion.getName()) == null) {
                 CardEdition cardEdition = new CardEdition();
                 String rarity = cardAttributes[9];
                 if (rarity.equals("Basic Land")) {
                     cardEdition.setRarity(CardRarity.BasicLand);
                 } else if (rarity.equals("Common")) {
                     cardEdition.setRarity(CardRarity.Common);
                 } else if (rarity.equals("Uncommon")) {
                     cardEdition.setRarity(CardRarity.Uncommon);
                 } else if (rarity.equals("Rare")) {
                     cardEdition.setRarity(CardRarity.Rare);
                 } else if (rarity.equals("Mythic Rare")) {
                     cardEdition.setRarity(CardRarity.MythicRare);
                 } else {
                     cardEdition.setRarity(CardRarity.Special);
                 }
 
                 cardEdition.setCardNumber(cardAttributes[10]);
                 cardEdition.setGathererId(cardAttributes[0]);
                 cardEdition.setCard(card);
                 cardEdition.setExpansion(expansion);
                 cardEditionRepository.persist(cardEdition);
             }
         }
     }
 
     @Transactional(readOnly = false)
     public CardStatus updateCardStatus(CardStatus cardStatus) {
         return cardStatusRepository.merge(cardStatus);
     }
 
     public Card findCardByName(String cardName) {
         return cardRepository.findByName(cardName);
     }
 
     public Card findCardById(long id) {
         return cardRepository.find(Card.class, id);
     }
 
     public List<CardEdition> getCardEditions(long cardId) {
         TypedQuery query = em.createNamedQuery(CardEdition.FIND_BY_CARD_ID, CardEdition.class);
         query.setParameter(1, cardId);
         return query.getResultList();
     }
 
     public List<ExpansionStatus> getExpansionStatus() {
         StringBuilder sb = new StringBuilder();
         sb.append("select e.name, e.code, count(*), ");
         sb.append("(select count(*)");
         sb.append(" from CardEdition cei inner join Card ci on ci.id = cei.card_id inner join CardStatus csi on csi.id = ci.status_id");
         sb.append(" where cei.expansion_id = e.id and csi.implemented=1)");
         sb.append(" from Expansion e inner join CardEdition ce on e.id = ce.expansion_id group by e.id order by e.releaseDate desc");
         Query query = em.createNativeQuery(sb.toString());
         List<Object[]> resultList = query.getResultList();
 
         List<ExpansionStatus> expansionsData = new LinkedList<ExpansionStatus>();
         for (Object[] object : resultList) {
             expansionsData.add(new ExpansionStatus((String) object[0], (String) object[1], ((BigInteger) object[2]).intValue(), ((BigInteger) object[3]).intValue()));
         }
         return expansionsData;
     }
 
     public List<Card> getCardsByCriteria(CardCriteria cardCriteria) {
         CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
         CriteriaQuery<Card> criteriaQuery = criteriaBuilder.createQuery(Card.class);
 
         Root<Card> card = criteriaQuery.from(Card.class);
         List<Predicate> restrictions = new LinkedList<Predicate>();
         if (cardCriteria.getAbilities() != null) {
             restrictions.add(criteriaBuilder.like(card.get(Card_.abilities), "%" + cardCriteria.getAbilities() + "%"));
         }
         if (cardCriteria.getExpansion() != null && !cardCriteria.getExpansion().isEmpty()) {
            Join<Card, CardEdition> cardEdition = card.join(Card_.editions);
             Join<CardEdition, Expansion> expansion = cardEdition.join(CardEdition_.expansion);
             In<String> inExpansion = criteriaBuilder.in(expansion.get(Expansion_.code));
             for (String value : cardCriteria.getExpansion()) {
                 inExpansion.value(value);
             }
             restrictions.add(inExpansion);
         }
         if (cardCriteria.getImplemented() != null || cardCriteria.getRequested() != null
                 || cardCriteria.getBugged() != null || cardCriteria.getTested() != null) {
             Join<Card, CardStatus> cardStatus = card.join(Card_.status);
             if (cardCriteria.getImplemented() != null) {
                 restrictions.add(criteriaBuilder.equal(cardStatus.get(CardStatus_.implemented), cardCriteria.getImplemented()));
             }
             if (cardCriteria.getRequested() != null) {
                 restrictions.add(criteriaBuilder.equal(cardStatus.get(CardStatus_.requested), cardCriteria.getRequested()));
             }
             if (cardCriteria.getBugged() != null) {
                 restrictions.add(criteriaBuilder.equal(cardStatus.get(CardStatus_.bugged), cardCriteria.getBugged()));
             }
             if (cardCriteria.getTested() != null) {
                 restrictions.add(criteriaBuilder.equal(cardStatus.get(CardStatus_.tested), cardCriteria.getTested()));
             }
         }
         criteriaQuery.where(restrictions.toArray(new Predicate[0]));
 
         criteriaQuery.orderBy(criteriaBuilder.asc(card.get("name")));
 
         criteriaQuery.select(card);
 
         TypedQuery<Card> query = em.createQuery(criteriaQuery);
 
         int page = cardCriteria.getPage();
         int rows = cardCriteria.getRows();
         query.setFirstResult(page * rows);
         query.setMaxResults(rows);
 
         return query.getResultList();
     }
 
     public Long getCardsCountByCriteria(CardCriteria cardCriteria) {
         CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
         CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
 
         Root<Card> card = criteriaQuery.from(Card.class);
         List<Predicate> restrictions = new LinkedList<Predicate>();
         if (cardCriteria.getAbilities() != null) {
             restrictions.add(criteriaBuilder.like(card.get(Card_.abilities), "%" + cardCriteria.getAbilities() + "%"));
         }
         if (cardCriteria.getExpansion() != null && !cardCriteria.getExpansion().isEmpty()) {
            Join<Card, CardEdition> cardEdition = card.join(Card_.editions);
             Join<CardEdition, Expansion> expansion = cardEdition.join(CardEdition_.expansion);
             In<String> inExpansion = criteriaBuilder.in(expansion.get(Expansion_.code));
             for (String value : cardCriteria.getExpansion()) {
                 inExpansion.value(value);
             }
             restrictions.add(inExpansion);
         }
         if (cardCriteria.getImplemented() != null || cardCriteria.getRequested() != null
                 || cardCriteria.getBugged() != null || cardCriteria.getTested() != null) {
             Join<Card, CardStatus> cardStatus = card.join(Card_.status);
             if (cardCriteria.getImplemented() != null) {
                 restrictions.add(criteriaBuilder.equal(cardStatus.get(CardStatus_.implemented), cardCriteria.getImplemented()));
             }
             if (cardCriteria.getRequested() != null) {
                 restrictions.add(criteriaBuilder.equal(cardStatus.get(CardStatus_.requested), cardCriteria.getRequested()));
             }
             if (cardCriteria.getBugged() != null) {
                 restrictions.add(criteriaBuilder.equal(cardStatus.get(CardStatus_.bugged), cardCriteria.getBugged()));
             }
             if (cardCriteria.getTested() != null) {
                 restrictions.add(criteriaBuilder.equal(cardStatus.get(CardStatus_.tested), cardCriteria.getTested()));
             }
         }
         criteriaQuery.where(restrictions.toArray(new Predicate[0]));
 
        criteriaQuery.select(criteriaBuilder.count(card));
         TypedQuery<Long> query = em.createQuery(criteriaQuery);
         return query.getSingleResult();
     }
 }
