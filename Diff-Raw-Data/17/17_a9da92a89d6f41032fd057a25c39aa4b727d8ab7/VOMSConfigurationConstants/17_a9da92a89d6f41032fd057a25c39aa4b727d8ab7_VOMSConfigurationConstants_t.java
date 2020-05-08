 /**
  * Copyright (c) Members of the EGEE Collaboration. 2006-2009.
  * See http://www.eu-egee.org/partners/ for details on the copyright holders.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * Authors:
  * 	Andrea Ceccanti (INFN)
  */
 
 package org.glite.security.voms.admin.configuration;
 
 public interface VOMSConfigurationConstants {
 
 	/**
 	 * Internal property used to hold vo name
 	 */
 	public static final String VO_NAME = "voms.vo.name";
 	
 	public static final String LOCALHOST_DEFAULTS_TO_LOCAL_ADMIN = "voms.localhost.defaults.to.local.admin";
 
 	public static final String READONLY = "voms.readonly";
 
 	public static final String VOMS_SERVICE_CERT = "voms.service.cert";
 	public static final String VOMS_SERVICE_KEY = "voms.service.key";
 	
	public static final String TRUST_ANCHORS_DIR = "voms.trust_anchors.dir";
	public static final String TRUST_ANCHORS_REFRESH_PERIOD = "voms.trust_anchors_refresh.period";
 	
 	public static final String CREATE_EXPIRED_CAS = "voms.ca.create-when-expired";
 	public static final String DROP_EXPIRED_CAS = "voms.ca.drop-when-expired";
 	
 	/** Shutdown properties **/
 	public static final String SHUTDOWN_PORT = "voms.shutdown_port";
 	public static final String SHUTDOWN_PASSWORD = "voms.shutdown_password";
 
 	public static final String SERVICE_EMAIL_ADDRESS = "voms.notification.email-address";
 	public static final String SERVICE_SMTP_SERVER = "voms.notification.smtp-server";
 	public static final String SERVICE_SMTP_SERVER_PORT = "voms.notification.smtp-server.port";
 	public static final String SERVICE_EMAIL_ACCOUNT_USERNAME = "voms.notification.username";
 	public static final String SERVICE_EMAIL_ACCOUNT_PASSWORD = "voms.notification.password";
 	public static final String SERVICE_EMAIL_USE_TLS = "voms.notification.use_tls";
 	public static final String NOTIFICATION_NOTIFY_BEHAVIOUR = "voms.notification.notify";
 	public static final String NOTIFICATION_RETRY_PERIOD = "voms.notification.retry_period";
 	public static final String NOTIFICATION_DISABLED = "voms.notification.disable";
 
 	public static final String VO_MEMBERSHIP_EXPIRED_REQ_PURGER_PERIOD = "voms.request.vo_membership.expired_request_purger.period";
 	public static final String UNCONFIRMED_REQUESTS_EXPIRATION_TIME = "voms.request.vo_membership.lifetime";
 	public static final String VO_MEMBERSHIP_UNCONFIRMED_REQ_WARN_POLICY = "voms.request.vo_membership.warn_when_expired";
 
 	/**
 	 * This configuration flag controls the user's ability to request attributes at VO registration time.
 	 */
 	public static final String VO_MEMBERSHIP_ENABLE_ATTRIBUTES_REQUEST = "voms.request.vo_membership.enable_attribute_requests";
 
 	public static final String USER_MAX_RESULTS_PER_PAGE = "voms.pagination.user.max.results.per.page";
 	public static final String ATTRIBUTES_MAX_RESULTS_PER_PAGE = "voms.pagination.attributes.max.results.per.page";
 	public static final String GROUP_MAX_RESULTS_PER_PAGE = "voms.pagination.group.max.results.per.page";
 	public static final String ROLE_MAX_RESULTS_PER_PAGE = "voms.pagination.role.max.results.per.page";
 	public static final String REGISTRATION_SERVICE_ENABLED = "voms.registration.enabled";
 	public static final String READ_ACCESS_FOR_AUTHENTICATED_CLIENTS = "voms.read-access-for-authenticated-clients";
 	/**
 	 * VERSION Properties
 	 */
 
 	public static final String VOMS_ADMIN_SERVER_VERSION = "voms-admin.server.version";
 	public static final String VOMS_ADMIN_INTERFACE_VERSION = "voms-admin.interface.version";
 
 	/**
 	 * AUP Properties
 	 */
 	public static final String GRID_AUP_URL = "voms.aup.grid_aup.initial_url";
 	public static final String VO_AUP_URL = "voms.aup.vo_aup.initial_url";
 	public static final String SIGN_AUP_TASK_LIFETIME = "voms.aup.sign_aup_task_lifetime";
 	public static final String REQUIRE_AUP_SIGNATURE_FOR_CREATED_USERS = "voms.aup.require_signature_for_created_users";
 	public static final String SIGN_AUP_EXTENDS_MEMBERSHIP = "voms.aup.signature_extends_membership";
 
 	/**
 	 * Membership Properties
 	 */
 	public static final String DEFAULT_MEMBERSHIP_LIFETIME = "voms.membership.default_lifetime";
 	public static final String MEMBERSHIP_CHECK_PERIOD = "voms.task.membership_check.period";
 	public static final String MEMBERSHIP_EXPIRATION_WARNING_PERIOD = "voms.membership.expiration_warning_period";
 	public static final String MEMBERSHIP_EXPIRATION_WARNING_PERIOD_DEFAULT_VALUE = "15";
 	public static final String MEMBERSHIP_EXPIRATION_GRACE_PERIOD = "voms.membership.expiration_grace_period";
 	public static final String NOTIFICATION_WARNING_RESEND_PERIOD = "voms.membership.notification_resend_period";
 	public static final String PRESERVE_EXPIRED_MEMBERS = "voms.preserve_expired_members";
 	public static final String DISABLE_MEMBERSHIP_END_TIME = "voms.disable_membership_end_time";
 	
 	
 	
 	
 	/**
 	 * Attribute Authority Properties
 	 */
 	public static final String VOMS_AA_CERT_FILE = "voms.aa.certificate";
 	public static final String VOMS_AA_KEY_FILE = "voms.aa.key";
 	public static final String VOMS_SAML_MAX_ASSERTION_LIFETIME = "voms.saml.max_assertion_lifetime";
 	public static final String VOMS_AA_COMPULSORY_GROUP_MEMBERSHIP = "voms.aa.compulsory_group_membership";
 	public static final String VOMS_AA_SAML_ACTIVATE_ENDPOINT = "voms.aa.activate_saml_endpoint";
 	public static final String VOMS_AA_REST_ACTIVATE_ENDPOINT = "voms.aa.activate_rest_endpoint";
 	public static final String VOMS_UNAUTHENTICATED_CLIENT_PERMISSION_MASK = "voms.unauthenticated_client_permission_mask";
 
 	/**
 	 * External validation plugin Properties
 	 */
 
 	public static final String VOMS_EXTERNAL_VALIDATOR_LIST = "voms.external-validators";
 	public static final String VOMS_EXTERNAL_VALIDATOR_PREFIX = "voms.ext";
 	public static final String VOMS_EXTERNAL_VALIDATOR_CONFIG_SUFFIX = "configClass";
 
 	/**
 	 * Internal registration properties
 	 */
 
 	public static final String VOMS_INTERNAL_REGISTRATION_TYPE = "___voms.regitration.type";
 	public static final String VOMS_INTERNAL_RO_PERSONAL_INFORMATION = "___voms.read-only-personal-information";
 	public static final String VOMS_INTERNAL_RO_MEMBERSHIP_EXPIRATION_DATE = "___voms.read-only-membership-expiration";
 
 	/**
 	 * Executor service properties
 	 */
 	public static final String THREAD_POOL_SIZE_PROPERTY = "voms.background_tasks.thread_pool_size";
 
 	/**
 	 * Axis Web services CSRF guard
 	 */
 	public static final String VOMS_CSRF_GUARD_LOG_ONLY = "voms.csrf.log_only";
 	
 	/**
 	 * VOMS Admin service hostname. This property is needed to properly compute the service
 	 * address when sending out email notifications.
 	 */
 	public static final String VOMS_SERVICE_HOSTNAME = "voms.hostname";
 	public static final String VOMS_SERVICE_PORT = "voms.port";
 
 	
 	/**
 	 * The local path where monitored stats properties will be stored. 
 	 */
 	public static final String MONITORING_USER_STATS_BASE_PATH = "voms.monitoring.user_stats_base_path";
 	
 	/**
 	 * The file name of the monitored stats properties.
 	 */
 	public static final String MONITORING_USER_STATS_FILE_NAME = "voms.monitoring.user_stats_filename";
 	
 	/**
 	 * How often (in minutes) the stats will be updated
 	 * 
 	 */
 	public static final String MONITORING_USER_STATS_UPDATE_PERIOD = "voms.monitoring.user_stats_period";
 
 }
