 package net.mademocratie.gae.server.entities.dto;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 import net.mademocratie.gae.server.entities.v1.Citizen;
 import net.mademocratie.gae.server.entities.v1.Proposal;
 import net.mademocratie.gae.server.services.helper.DateHelper;
 
 import java.util.Date;
 
 public class ProposalDTO extends ContributionDTO {
     private String title;
     private String content;
 
     public ProposalDTO() {
     }
 
     public ProposalDTO(Citizen author, Proposal proposal) {
         super(author, proposal);
         this.content = proposal.getContent();
         this.title = proposal.getTitle();
     }
 
     public String getContent() {
         return content;
     }
 
     public void setContent(String content) {
         this.content = content;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
     @Override                   // json need id
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
 
     @JsonProperty
     public CitizenDTO getAuthor() {
         return super.getAuthor();
     }
 
     @Override                    // json need id
     public Long getContributionId() {
         return super.getContributionId();
     }
 
     @Override
     public String getContributionDetails() {
         return "create proposition '" + getTitle() + "'";
     }
 
     @Override
     public String getContributionType() {
         return ContributionType.PROPOSAL.toString();
     }
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder("ProposalDTO{");
        sb.append(super.toString());
        sb.append(", content='").append(content).append('\'');
         sb.append(", title='").append(title).append('\'');
         sb.append('}');
         return sb.toString();
     }
 }
