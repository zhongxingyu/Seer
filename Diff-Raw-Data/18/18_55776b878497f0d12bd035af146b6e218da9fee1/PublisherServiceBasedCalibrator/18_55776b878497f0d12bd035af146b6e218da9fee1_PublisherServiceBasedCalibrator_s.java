 package org.hbird.application.calibration;
 
 import org.hbird.core.spacesystemmodel.exceptions.CalibrationException;
 import org.hbird.core.spacesystemmodel.tmtc.Parameter;
 import org.hbird.core.spacesystempublisher.interfaces.SpaceSystemPublisher;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class PublisherServiceBasedCalibrator {
 	private static final Logger LOG = LoggerFactory.getLogger(PublisherServiceBasedCalibrator.class);
 
 	private SpaceSystemPublisher publisherService;
 
 	private InMemoryCalibrator calibrator;
 
	public void cacheModelInformation() {
 		if (publisherService != null) {
 			calibrator = new InMemoryCalibrator(publisherService.getAllCalibrators());
 		}
 		else {
 			LOG.warn("Attempt to cache space system model information when there is no publisher service available");
 		}
 	}
 
 	public Parameter<?> calibrate(Parameter<?> parameter) throws CalibrationException {
 		return calibrator.calibrate(parameter);
 	}
 
 	public SpaceSystemPublisher getPublisherService() {
 		return publisherService;
 	}
 
 	public void setPublisherService(SpaceSystemPublisher publisherService) {
 		this.publisherService = publisherService;
 	}
 }
