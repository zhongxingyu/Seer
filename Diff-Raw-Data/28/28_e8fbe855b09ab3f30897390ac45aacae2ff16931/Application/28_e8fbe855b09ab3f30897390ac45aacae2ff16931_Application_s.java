 package org.atlasapi.application;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import org.joda.time.DateTime;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Preconditions;
 
 public class Application {
 
     public static final Builder application(String slug) {
         return new Builder(checkNotNull(slug));
     }
     
     public static class Builder {
 
         private final String slug;
         private String title;
         private String desc;
         private DateTime created;
         private ApplicationConfiguration config = ApplicationConfiguration.DEFAULT_CONFIGURATION;
         private ApplicationCredentials creds;
 
         public Builder(String slug) {
             this.slug = slug;
         }
         
         public Builder withTitle(String title) {
             this.title = title;
             return this;
         }
         
         public Builder withDescription(String desc) {
             this.desc = desc;
             return this;
         }
         
         public Builder createdAt(DateTime time) {
             this.created = time;
             return this;
         }
         
         public Builder withConfiguration(ApplicationConfiguration config) {
             this.config = config;
             return this;
         }
         
         public Builder withCredentials(ApplicationCredentials creds) {
             this.creds = creds;
             return this;
         }
         
         public Application build() {
             Preconditions.checkState(creds != null, "Application credentials must be set");
             Preconditions.checkState(config != null, "Application configuration must be set");
            return new Application(slug, title, desc, created, config, creds);
         }
     }
     
 	private final String slug;
 	private final String title;
 	private final String description;
 	private final DateTime created;
 
 	private final ApplicationConfiguration configuration;
 	private final ApplicationCredentials credentials;
 
	private Application(String slug, String title, String desc, DateTime created, ApplicationConfiguration config, ApplicationCredentials creds) {
 		this.slug = slug;
         this.title = title;
         this.description = desc;
         this.created = created;
         this.configuration = config;
         this.credentials = creds;
 	}
 
 	public String getSlug() {
 		return slug;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 	
 	public ApplicationConfiguration getConfiguration() {
 		return configuration;
 	}
 
 	public ApplicationCredentials getCredentials() {
 		return credentials;
 	}
 	
     public String getDescription() {
         return description;
     }
 
     public DateTime getCreated() {
         return created;
     }
     
     public Builder copy() {
         return new Builder(slug)
             .withTitle(title)
             .withDescription(description)
             .createdAt(created)
             .withConfiguration(configuration)
            .withCredentials(credentials);
     }
 
 	@Override
 	public int hashCode() {
 		return Objects.hashCode(getSlug());
 	}
 
 	@Override
 	public boolean equals(Object that) {
 	    if (this == that) {
 	        return true;
 	    }
 		if (that instanceof Application) {
 			Application other = (Application) that;
 			return slug.equals(other.slug);
 		}
 		return false;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("%s (%s)", getSlug(), getTitle());
 	}
 }
