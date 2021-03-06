 /*
  * Copyright 2009 Sven Strickroth <email@cs-ware.de>
  * 
  * This file is part of the SubmissionInterface.
  * 
  * SubmissionInterface is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3 as
  * published by the Free Software Foundation.
  * 
  * SubmissionInterface is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SubmissionInterface. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.tuclausthal.submissioninterface.servlets.controller;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
 import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
 import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
 import de.tuclausthal.submissioninterface.persistence.dao.PointsDAOIf;
 import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
 import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
 import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
 import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
 import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
 import de.tuclausthal.submissioninterface.util.ContextAdapter;
 import de.tuclausthal.submissioninterface.util.Util;
 
 /**
  * Controller-Servlet for loading and displaying a submission to tutors
  * @author Sven Strickroth
  */
 public class ShowSubmission extends HttpServlet {
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf();
 		Submission submission = submissionDAO.getSubmission(Util.parseInteger(request.getParameter("sid"), 0));
 		if (submission == null) {
 			request.setAttribute("title", "Abgabe nicht gefunden");
 			request.getRequestDispatcher("MessageView").forward(request, response);
 			return;
 		}
 
 		Task task = submission.getTask();
 
 		// check Lecture Participation
 		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf();
 		Participation participation = participationDAO.getParticipation(new SessionAdapter(request).getUser(), submission.getTask().getLecture());
 		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
 			((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
 			return;
 		}
 
 		if (task.getDeadline().before(Util.correctTimezone(new Date())) && request.getParameter("points") != null) {
 			PointsDAOIf pointsDAO = DAOFactory.PointsDAOIf();
 			pointsDAO.createPoints(Util.parseInteger(request.getParameter("points"), 0), submission, participation);
 			response.sendRedirect(response.encodeRedirectURL("ShowSubmission?sid=" + submission.getSubmissionid()));
 			return;
 		}
 
 		List<String> submittedFiles = new LinkedList<String>();
 		ContextAdapter contextAdapter = new ContextAdapter(getServletContext());
 		File path = new File(contextAdapter.getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator"));
 		if (path.listFiles() != null) {
 			for (File file : path.listFiles()) {
				if (file.isFile() && (file.getName().endsWith(".java") || file.getName().endsWith(".txt"))) {
 					submittedFiles.add(file.getName());
 				}
 			}
 		}
 
 		request.setAttribute("submission", submission);
 		request.setAttribute("submittedFiles", submittedFiles);
 		request.getRequestDispatcher("ShowSubmissionView").forward(request, response);
 	}
 }
