 package org.palaso.languageforge.client.lex.service.actions;
 
 import org.palaso.languageforge.client.lex.common.BaseConfiguration;
 import org.palaso.languageforge.client.lex.jsonrpc.JsonRpcAction;
 import org.palaso.languageforge.client.lex.model.CurrentEnvironmentDto;
 import org.palaso.languageforge.client.lex.model.UserListDto;
 
 public class GetUsersListAction extends JsonRpcAction<UserListDto> {
 
 	public GetUsersListAction() {
 		super(BaseConfiguration.getInstance().getLFApiPath(), BaseConfiguration.getInstance().getApiFileName(), "listUsersInProject",1);
 	}
 
 	@Override
 	public String encodeParam(int i) {
		return String.valueOf(CurrentEnvironmentDto.getCurrentProject().getProjectId());
 	}
 
 }
