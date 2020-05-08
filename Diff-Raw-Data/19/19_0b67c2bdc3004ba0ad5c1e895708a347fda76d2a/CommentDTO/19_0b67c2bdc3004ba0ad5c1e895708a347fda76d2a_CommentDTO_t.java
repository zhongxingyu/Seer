 package net.mademocratie.gae.server.entities.dto;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 import net.mademocratie.gae.server.entities.v1.Citizen;
 import net.mademocratie.gae.server.entities.v1.Comment;
 import net.mademocratie.gae.server.entities.v1.Contribution;
 import net.mademocratie.gae.server.services.helper.DateHelper;
 
 import java.util.Date;
 
 public class CommentDTO extends ContributionDTO {
     protected Contribution parentContribution;
     private String parentContributionType = ContributionType.PROPOSAL.toString();
     private String content;
 
     public CommentDTO() {
     }
 
     public CommentDTO(Citizen author, Comment comment, Contribution parentContribution) {
         super(author, comment);
         this.parentContribution = parentContribution;
         this.parentContributionType = comment.getParentContributionType();
         this.content = comment.getContent();
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
 
     @Override
     public Long getContributionId() {
         return (parentContribution != null ? parentContribution.getContributionId() : null);
     }
 
     @Override
     public String getContributionType() {
         return ContributionType.COMMENT.toString();
     }
 
     @Override
     public String getContributionDetails() {
         return "comment on '" + parentContributionType + "'";
     }
 
     public String getContent() {
         return content;
     }
 
     public void setContent(String content) {
         this.content = content;
     }
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder("CommentDTO{");
        sb.append(super.toString());
        sb.append(", content='").append(content).append('\'');
         sb.append(", parentContribution=").append(parentContribution);
         sb.append(", parentContributionType='").append(parentContributionType).append('\'');
         sb.append('}');
         return sb.toString();
     }
 }
