 package com.leonti.slickpm.controller;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.leonti.slickpm.domain.Comment;
 import com.leonti.slickpm.domain.GitVcs;
 import com.leonti.slickpm.domain.Task;
import com.leonti.slickpm.domain.TaskType;
 import com.leonti.slickpm.domain.UploadedFile;
 import com.leonti.slickpm.domain.dto.CommentDTO;
 import com.leonti.slickpm.domain.dto.TaskDTO;
 import com.leonti.slickpm.domain.dto.UploadedFileDTO;
 import com.leonti.slickpm.domain.dto.VcsCommit;
 import com.leonti.slickpm.domain.dto.VcsDiff;
 import com.leonti.slickpm.service.CommentService;
 import com.leonti.slickpm.service.ProjectService;
 import com.leonti.slickpm.service.TaskService;
 import com.leonti.slickpm.service.TaskTypeService;
 import com.leonti.slickpm.service.UploadedFileService;
 import com.leonti.slickpm.service.UserService;
 import com.leonti.slickpm.service.VcsService;
 
 @Controller
 @Secured("ROLE_USER")
 @RequestMapping("/task")
 public class TaskController {
 
 	@Resource(name = "TaskService")
 	TaskService taskService;
 
 	@Resource(name = "TaskTypeService")
 	TaskTypeService taskTypeService;
 
 	@Resource(name = "ProjectService")
 	ProjectService projectService;
 
 	@Resource(name = "UserService")
 	UserService userService;
 
 	@Resource(name = "CommentService")
 	CommentService commentService;
 
 	@Resource(name = "UploadedFileService")
 	UploadedFileService uploadedFileService;
 
 	@Resource(name = "VcsService")
 	VcsService vcsService;
 
 	@RequestMapping(value = "{id}", method = RequestMethod.GET)
 	public @ResponseBody
 	TaskDTO RESTDetails(@PathVariable("id") Integer id) {
 
 		return taskService.getById(id).getDTO();
 	}
 
 	@RequestMapping(value = "", method = RequestMethod.POST)
 	@ResponseBody
 	public TaskDTO RESTAdd(@RequestBody TaskDTO taskDTO) {
 
 		Task task = new Task();
 
		
 		task.setTitle(taskDTO.getTitle());
 		task.setDescription(taskDTO.getDescription());
 		task.setProject(projectService.getById(taskDTO.getProject().getId()));

		for (TaskType taskType : taskTypeService.getList()) {
			if (taskType.getTitle().contains("User")) {
				task.setTaskType(taskType);
			}
		}
 		taskService.save(task);
 
 		return task.getDTO();
 	}
 
 	@RequestMapping(value = "{id}", method = RequestMethod.PUT)
 	@ResponseBody
 	public TaskDTO RESTUpdate(@RequestBody TaskDTO taskDTO,
 			@PathVariable("id") Integer id) {
 
 		Task task = taskService.getById(taskDTO.getId());
 		task.setTitle(taskDTO.getTitle());
 		task.setDescription(taskDTO.getDescription());
 		task.setPoints(taskDTO.getPoints());
 
 		if (taskDTO.getUser() != null) {
 			task.setUser(userService.getById(taskDTO.getUser().getId()));
 		} else {
 			task.setUser(null);
 		}
 
 		if (taskDTO.getTaskType() != null) {
 			task.setTaskType(taskTypeService.getById(taskDTO.getTaskType()
 					.getId()));
 		} else {
 			task.setTaskType(null);
 		}
 
 		taskService.save(task);
 
 		return taskDTO;
 	}
 
 	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
 	@ResponseBody
 	public void RESTDelete(@PathVariable("id") Integer id) {
 
 		taskService.delete(taskService.getById(id));
 	}
 
 	@RequestMapping(value = "{taskId}/comment", method = RequestMethod.GET)
 	public @ResponseBody
 	List<CommentDTO> RESTCommentList(@PathVariable("taskId") Integer taskId) {
 
 		List<CommentDTO> comments = new ArrayList<CommentDTO>();
 		for (Comment comment : commentService.getList(taskId)) {
 			comments.add(comment.getDTO());
 		}
 
 		return comments;
 	}
 
 	@RequestMapping(value = "{taskId}/comment/{id}", method = RequestMethod.GET)
 	public @ResponseBody
 	CommentDTO RESTGetComment(@PathVariable("id") Integer id) {
 		return commentService.getById(id).getDTO();
 	}
 
 	@RequestMapping(value = "{taskId}/comment", method = RequestMethod.POST)
 	@ResponseBody
 	public CommentDTO RESTAddComment(@PathVariable("taskId") Integer taskId,
 			@RequestBody CommentDTO commentDTO) {
 
 		Comment comment = new Comment();
 
 		comment.setContent(commentDTO.getContent());
 		comment.setDate(new Date());
 		comment.setTask(taskService.getById(taskId));
 		commentService.save(comment);
 
 		return comment.getDTO();
 	}
 
 	@RequestMapping(value = "{taskId}/comment/{id}", method = RequestMethod.PUT)
 	@ResponseBody
 	public CommentDTO RESTUpdateComment(@PathVariable("taskId") Integer taskId,
 			@RequestBody CommentDTO commentDTO, @PathVariable("id") Integer id) {
 
 		Comment comment = commentService.getById(id);
 
 		comment.setContent(commentDTO.getContent());
 		commentService.save(comment);
 
 		return comment.getDTO();
 	}
 
 	@RequestMapping(value = "{taskId}/comment/{id}", method = RequestMethod.DELETE)
 	@ResponseBody
 	public void RESTDeleteComment(@PathVariable("id") Integer id) {
 
 		commentService.delete(commentService.getById(id));
 	}
 
 	@RequestMapping(value = "{taskId}/file", method = RequestMethod.GET)
 	public @ResponseBody
 	List<UploadedFileDTO> RESTFiletList(@PathVariable("taskId") Integer taskId) {
 
 		Task task = taskService.getById(taskId);
 		List<UploadedFileDTO> files = new ArrayList<UploadedFileDTO>();
 		for (UploadedFile uploadedFile : task.getFiles()) {
 			files.add(uploadedFile.getDTO());
 		}
 
 		return files;
 	}
 
 	@RequestMapping(value = "{taskId}/addFile", method = RequestMethod.POST)
 	public @ResponseBody
 	Map<String, String> addFile(@PathVariable("taskId") Integer taskId,
 			@RequestParam(value = "id", required = true) Integer id) {
 
 		taskService.addUploadedFile(taskService.getById(taskId),
 				uploadedFileService.getById(id));
 
 		Map<String, String> result = new HashMap<String, String>();
 		result.put("result", "OK");
 
 		return result;
 	}
 
 	@RequestMapping(value = "{taskId}/dependencyCandidates", method = RequestMethod.GET)
 	public @ResponseBody
 	List<TaskDTO> dependencyCandidates(@PathVariable("taskId") Integer taskId) {
 
 		List<TaskDTO> tasks = new ArrayList<TaskDTO>();
 
 		for (Task task : taskService.getDependencyCandidates(taskService
 				.getById(taskId))) {
 			tasks.add(task.getDTO());
 		}
 
 		return tasks;
 	}
 
 	@RequestMapping(value = "{taskId}/dependency", method = RequestMethod.GET)
 	public @ResponseBody
 	List<TaskDTO> dependencyList(@PathVariable("taskId") Integer taskId) {
 
 		List<TaskDTO> tasks = new ArrayList<TaskDTO>();
 
 		for (Task task : taskService.getById(taskId).getDependsOn()) {
 			tasks.add(task.getDTO());
 		}
 
 		return tasks;
 	}
 
 	@RequestMapping(value = "{taskId}/addDependency", method = RequestMethod.POST)
 	public @ResponseBody
 	Map<String, String> addDependency(
 			@PathVariable("taskId") Integer taskId,
 			@RequestParam(value = "dependencyId", required = true) Integer dependencyId) {
 
 		taskService.addDependency(taskService.getById(taskId),
 				taskService.getById(dependencyId));
 
 		Map<String, String> result = new HashMap<String, String>();
 		result.put("result", "OK");
 
 		return result;
 	}
 
 	@RequestMapping(value = "{taskId}/removeDependency", method = RequestMethod.POST)
 	public @ResponseBody
 	Map<String, String> removeDependency(
 			@PathVariable("taskId") Integer taskId,
 			@RequestParam(value = "dependencyId", required = true) Integer dependencyId) {
 
 		taskService.removeDependency(taskService.getById(taskId),
 				taskService.getById(dependencyId));
 
 		Map<String, String> result = new HashMap<String, String>();
 		result.put("result", "OK");
 
 		return result;
 	}
 
 	@RequestMapping(value = "{taskId}/vcsDiff", method = RequestMethod.GET)
 	public @ResponseBody
 	List<VcsCommit> getVcsDiff(@PathVariable("taskId") Integer taskId) {
 
 		Task task = taskService.getById(taskId);
 		List<VcsCommit> commits = vcsService.getDiffForTask((GitVcs) task
 				.getProject().getVcs(), task);
 
 		for (VcsCommit commit : commits) {
 
 			for (VcsDiff diff : commit.getDiffs()) {
 
 				diff.setContent(StringEscapeUtils.escapeHtml4(diff.getContent()));
 			}
 		}
 
 		return commits;
 	}
 }
