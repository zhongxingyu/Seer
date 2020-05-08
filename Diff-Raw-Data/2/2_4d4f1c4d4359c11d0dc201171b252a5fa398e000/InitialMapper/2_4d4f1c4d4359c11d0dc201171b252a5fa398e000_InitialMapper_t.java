 package net.thumbtack.updateNotifierBackend.database.mappers;
 
 import org.apache.ibatis.annotations.Update;
 
 public interface InitialMapper {
 
 	String CREATE_USER_INC = "DO $$ BEGIN IF NOT EXISTS (SELECT 0 FROM pg_class where relname = 'auto_id_users' ) THEN CREATE SEQUENCE auto_id_users; END IF; END $$";
 	String CREATE_USER_TBL = "CREATE TABLE IF NOT EXISTS users(id BIGINT NOT NULL DEFAULT nextval('auto_id_users'), name varchar(30) not null, surname varchar(30) not null, email VARCHAR(50) NOT NULL, PRIMARY KEY (id))";
 	String CREATE_RES_INC = "DO $$ BEGIN IF NOT EXISTS (SELECT 0 FROM pg_class where relname = 'auto_id_res' ) THEN CREATE SEQUENCE auto_id_res; END IF; END $$";
	String CREATE_RES_TBL = "CREATE TABLE IF NOT EXISTS resources(id bigint NOT NULL DEFAULT nextval('auto_id_res'), user_id bigint NOT NULL, name varchar(50) NOT NULL, url varchar(255) NOT NULL, schedule_code smallint NOT NULL, filter varchar(255), hash integer NOT NULL, last_update timestamp NOT NULL, PRIMARY KEY (id), FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)";
 	String CREATE_TAG_INC = "DO $$ BEGIN IF NOT EXISTS (SELECT 0 FROM pg_class where relname = 'auto_id_tags' ) THEN CREATE SEQUENCE auto_id_tags; END IF; END $$";
 	String CREATE_TAG_TBL = "CREATE TABLE IF NOT EXISTS tags(id bigint NOT NULL DEFAULT nextval('auto_id_tags'), user_id bigint NOT NULL, name varchar(30) NOT NULL, PRIMARY KEY (id), CONSTRAINT no_duplicate_tags UNIQUE (user_id, name), FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)";
 	String CREATE_RES_TAG_TBL = "CREATE TABLE IF NOT EXISTS resource_tag(resource_id bigint, tag_id bigint, PRIMARY KEY (tag_id, resource_id), FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE, FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE)";
 	
 	@Update(CREATE_USER_INC)
 	int createUserCounter();
 	
 	@Update(CREATE_USER_TBL)
 	int createUserTable();
 
 	@Update(CREATE_RES_INC)
 	void createResourceCounter();
 
 	@Update(CREATE_RES_TBL)
 	void createResourceTable();
 
 	@Update(CREATE_TAG_INC)
 	void createTagCounter();
 
 	@Update(CREATE_TAG_TBL)
 	void createTagTable();
 
 	@Update(CREATE_RES_TAG_TBL)
 	void createResourceTagTable();
 	
 	
 }
