 package com.twistlet.falcon.model.service;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang3.BooleanUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.time.DateUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.twistlet.falcon.model.entity.FalconAppointment;
 import com.twistlet.falcon.model.entity.FalconAppointmentPatron;
 import com.twistlet.falcon.model.entity.FalconLocation;
 import com.twistlet.falcon.model.entity.FalconPatron;
 import com.twistlet.falcon.model.entity.FalconService;
 import com.twistlet.falcon.model.entity.FalconStaff;
 import com.twistlet.falcon.model.entity.FalconUser;
 import com.twistlet.falcon.model.repository.FalconAppointmentRepository;
 import com.twistlet.falcon.model.repository.FalconUserRepository;
 
 @Service("reminderService")
 public class ReminderServiceImpl implements ReminderService {
 
 	private final FalconAppointmentRepository falconAppointmentRepository;
 	private final FalconUserRepository falconUserRepository;
 	private final MailSenderService mailSenderService;
 	private final SmsService smsService;
 	private final String message;
 
 	protected final Logger logger = LoggerFactory.getLogger(getClass());
 
 	@Autowired
 	public ReminderServiceImpl(final FalconUserRepository falconUserRepository,
 			final FalconAppointmentRepository falconAppointmentRepository, final MailSenderService mailSenderService,
 			final SmsService smsService, @Value("${mail.content.reminder}") final String messageLocation) {
 		this.falconUserRepository = falconUserRepository;
 		this.falconAppointmentRepository = falconAppointmentRepository;
 		this.mailSenderService = mailSenderService;
 		this.smsService = smsService;
 		try {
 			message = StringUtils.join(FileUtils.readLines(new ClassPathResource(messageLocation).getFile()), "\n");
 		} catch (final IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public List<FalconAppointment> listAppointmentsNeedingReminders(final long seconds) {
 		final Date now = new Date();
 		final Date maxDate = DateUtils.addSeconds(now, new Long(seconds).intValue());
 		return falconAppointmentRepository.findByAppointmentDateBetweenAndNotified(now, maxDate, 'N');
 	}
 
 	public void sendNotification(final FalconAppointment falconAppointment) {
 		final SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");
 		final SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm:ss aa");
 		final Date dateTime = falconAppointment.getAppointmentDate();
 		final FalconStaff falconStaff = falconAppointment.getFalconStaff();
 		final FalconLocation falconLocation = falconAppointment.getFalconLocation();
 		final FalconService falconService = falconAppointment.getFalconService();
 		final Set<FalconAppointmentPatron> falconPatrons = falconAppointment.getFalconAppointmentPatrons();
 		final String date = formatDate.format(dateTime);
 		final String time = formatTime.format(dateTime);
 		final String staff = falconStaff.getName();
 		final String venue = falconLocation.getName();
 		final String service = falconService.getName();
 		final String subject = "Your scheduled appointment is due soon";
 		final String smsFormat = "Appointment due soon! {0}, {1}, {2}, {3}, {4}, {5}";
 		for (final FalconAppointmentPatron falconAppointmentPatron : falconPatrons) {
 			sendToPatron(falconAppointment, date, time, staff, venue, service, subject, smsFormat, falconAppointmentPatron);
 		}
 		sendToStaff(date, time, staff, venue, service, smsFormat, subject, falconStaff, falconAppointment);
 		falconAppointment.setNotified('Y');
 		falconAppointmentRepository.save(falconAppointment);
 	}
 
 	private void sendToStaff(final String date, final String time, final String staff, final String venue, final String service,
 			final String smsFormat, final String subject, final FalconStaff falconStaff, final FalconAppointment falconAppointment) {
 		final FalconUser falconAdmin = falconStaff.getFalconUser();
 		final String sender = falconAdmin.getEmail();
 		final String target = falconStaff.getEmail();
 		final Set<FalconAppointmentPatron> set = falconAppointment.getFalconAppointmentPatrons();
 		final String patron;
 		if (set.size() == 1) {
 			final ArrayList<FalconAppointmentPatron> list = new ArrayList<>(set);
 			final FalconAppointmentPatron item = list.get(0);
 			final FalconPatron falconPatron = item.getFalconPatron();
 			final FalconUser falconUser = falconPatron.getFalconUserByPatron();
 			patron = falconUser.getName();
 		} else {
			patron = "Patron Group #" + falconAppointment.getId();
 		}
 		final Object[] arguments = { date, time, staff, patron, venue, service };
 		if (BooleanUtils.toBoolean(falconStaff.getSendEmail())) {
 			final String mailContent = MessageFormat.format(message, arguments);
 			try {
 				mailSenderService.send(sender, target, mailContent, subject);
 			} catch (final Exception e) {
 				e.printStackTrace();
 			}
 		} else {
 			logger.info("{}, {} mail not sent. The staff settings is no mail.", falconAppointment.getId(), target);
 		}
 		if (BooleanUtils.toBoolean(falconStaff.getSendSms())) {
 			final String smsContent = MessageFormat.format(smsFormat, arguments);
 			final int smsRemaining = falconAdmin.getSmsRemaining();
 			if (smsRemaining > 0) {
 				smsService.send(sender, falconStaff.getHpTel(), smsContent);
 				falconAdmin.setSmsRemaining(smsRemaining - 1);
 				final int smsSent = falconAdmin.getSmsSentLifetime();
 				falconAdmin.setSmsSentLifetime(smsSent + 1);
 				falconUserRepository.save(falconAdmin);
 			} else {
 				logger.warn("{}, {} sms not sent. The admin '{}' ran out of sms credits.", falconAppointment.getId(),
 						falconStaff.getEmail(), falconAdmin.getUsername());
 			}
 		} else {
 			logger.info("{}, {} sms not sent. The staff settings is no sms.", falconAppointment.getId(), falconStaff.getEmail());
 		}
 	}
 
 	private void sendToPatron(final FalconAppointment falconAppointment, final String date, final String time, final String staff,
 			final String venue, final String service, final String subject, final String smsFormat,
 			final FalconAppointmentPatron falconAppointmentPatron) {
 		final FalconPatron falconPatron = falconAppointmentPatron.getFalconPatron();
 		final FalconUser thePatron = falconPatron.getFalconUserByPatron();
 		final FalconUser theAdmin = falconPatron.getFalconUserByAdmin();
 		final String sender = theAdmin.getUsername();
 		final String patron = thePatron.getName();
 		final Object[] arguments = { date, time, staff, patron, venue, service };
 		final String mailContent = MessageFormat.format(message, arguments);
 		final String smsContent = MessageFormat.format(smsFormat, arguments);
 		if (BooleanUtils.toBoolean(thePatron.getSendEmail())) {
 			try {
 				mailSenderService.send(sender, thePatron.getEmail(), mailContent, subject);
 			} catch (final Exception e) {
 				e.printStackTrace();
 			}
 		} else {
 			logger.info("{}, {} mail not sent. The patron settings is no mail.", falconAppointment.getId(), thePatron.getUsername());
 		}
 		if (BooleanUtils.toBoolean(thePatron.getSendSms())) {
 			final int smsRemaining = theAdmin.getSmsRemaining();
 			if (smsRemaining > 0) {
 				smsService.send(sender, thePatron.getPhone(), smsContent);
 				theAdmin.setSmsRemaining(smsRemaining - 1);
 				final int smsSent = theAdmin.getSmsSentLifetime();
 				theAdmin.setSmsSentLifetime(smsSent + 1);
 				falconUserRepository.save(theAdmin);
 			} else {
 				logger.warn("{}, {} sms not sent. The admin '{}' ran out of sms credits.", falconAppointment.getId(),
 						thePatron.getUsername(), theAdmin.getUsername());
 			}
 		} else {
 			logger.info("{}, {} sms not sent. The patron settings is no sms.", falconAppointment.getId(), thePatron.getUsername());
 		}
 	}
 
 	@Override
 	@Transactional
 	public void sendNotificationToAppointmentsInTheFuture(final long seconds) {
 		final List<FalconAppointment> list = listAppointmentsNeedingReminders(seconds);
 		for (final FalconAppointment falconAppointment : list) {
 			sendNotification(falconAppointment);
 		}
 	}
 }
