 package net.mademocratie.gae.server.entities.v1;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.Index;
 import net.mademocratie.gae.server.entities.IContribution;
 import net.mademocratie.gae.server.services.helper.DateHelper;
 
 import javax.xml.bind.annotation.XmlRootElement;
 import java.util.Date;
 
 @XmlRootElement
 @Entity
 public class Vote extends Contribution implements IContribution {
     @Index
     protected Key<Proposal> proposal;
 
     protected VoteKind kind;
 
     public Vote() {}
 
     public Vote(Citizen author, Proposal proposal, VoteKind kind) {
         super(author);
         this.kind = kind;
         this.proposal = Key.create(Proposal.class, proposal.getContributionId());
     }
 
     public Vote(Vote v) {
         super(v);
         this.setKind(v.getKind());
        this.proposal = v.getProposal();
     }
 
     @Override
     public Date getDate() {
         return super.getDate();
     }
 
     @Override
     public String getDateFormat() {
         return DateHelper.getDateFormat(getDate());
     }
 
     @Override
     public String getAge() {
         return super.getAge();
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
 
     public VoteKind getKind() {
         return kind;
     }
 
     public void setKind(VoteKind kind) {
         this.kind = kind;
     }
 
     public Key<Proposal> getProposal() {
         return proposal;
     }
 
     public Long getProposalId() {
         return proposal.getId();
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("[Vote");
         if (getContributionId() != null) {
             sb.append("#").append(getContributionId());
         }
         sb.append(":").append(getDate());
         sb.append("|").append(getKind())
                 .append(" by citizen#").append(getAuthor())
                 .append(" on proposal#").append(getProposalId())
                 .append("]");
         return sb.toString();
     }
 
     @Override                    // json need id
     public Long getContributionId() {
         return proposal.getId();
     }
 
 
     @Override
     public String getContributionType() {
         return ContributionType.VOTE.toString();
     }
 
     @Override
     public String getContributionDetails() {
         return "vote on proposal '" + getProposalId() + "'";
     }
 
     public void setProposal(Key<Proposal> proposal) {
         this.proposal = proposal;
     }
 }
